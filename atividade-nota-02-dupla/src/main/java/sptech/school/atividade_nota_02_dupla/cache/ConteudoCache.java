package sptech.school.atividade_nota_02_dupla.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ConteudoCache
 *
 * <p>Ele guarda os conteúdos em memória usando um {@link ConcurrentHashMap}:
 * <ul>
 *     <li>chave: {@code Long id}</li>
 *     <li>valor: {@link EntradaCache} (conteúdo + data de armazenamento + último acesso)</li>
 * </ul>
 *
 * <p>Características implementadas:
 * <ul>
 *     <li><b>TTL</b>: item mais velho que o TTL configurado vira MISS;</li>
 *     <li><b>LRU</b>: quando o cache está cheio, sai o item usado há mais tempo;</li>
 *     <li><b>Invalidação</b>: ao atualizar/excluir, a entrada é removida;</li>
 *     <li><b>Estatísticas</b>: hits, misses e evictions;</li>
 *     <li><b>Logs</b>: cada consulta gera um log HIT/MISS com id e tempo em ms.</li>
 * </ul>
 *
 */
@Component
public class ConteudoCache {

    private static final Logger log = LoggerFactory.getLogger(ConteudoCache.class);

    // Mapa principal do cache (id junto a uma EntradaCache com o conteúdo)
    private final Map<Long, EntradaCache> cache = new ConcurrentHashMap<>();

    // Limite máximo de itens no cache
    private final int maxItens;

    // Tempo de vida de cada item (application.properties)
    private final long ttlSegundos;

    // Contadores de estatísticas (AtomicLong faz com que, caso haja 2 ou mais threads
    // usadas ao mesmo tempo, seja adicionado +1 sem qualquer conflito com o valor).
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);

    // Ponto de injeção de valores vindos do application.properties
    public ConteudoCache(@Value("${app.cache.max-itens:5}") int maxItens,
                          @Value("${app.cache.ttl-segundos:60}") long ttlSegundos) {
        this.maxItens = maxItens;
        this.ttlSegundos = ttlSegundos;
        log.info("ConteudoCache iniciado - limite={} itens, TTL={}s", this.maxItens, this.ttlSegundos);
    }


     // Metodo que:
     // - olha se o id existe no mapa (se não existir -> MISS)
     // - olha se o item expirou pelo TTL (se expirou -> remove e conta MISS)
     // - se estiver válido -> atualiza o "último acesso" e conta HIT.

     // O log de HIT/MISS (com o tempo gasto) é feito aqui.
     // Aqui sabemos se houve um HIT ou MISS

     // id -> identificador do conteúdo procurado.
     // return -> retorna o conteúdo se deu HIT (ou Optional.empty() se deu MISS).

    public Optional<ConteudoCacheItem> buscar(Long id) {
        long inicio = System.currentTimeMillis();
        EntradaCache entrada = cache.get(id);

        if (entrada == null) {
            misses.incrementAndGet();
            long tempo = System.currentTimeMillis() - inicio;
            log.info("Cache MISS - id={} - {} ms", id, tempo);
            return Optional.empty();
        }

        // O item existe: será que ele já passou do TTL?
        if (entrada.isExpirado(ttlSegundos)) {
            cache.remove(id);
            misses.incrementAndGet();
            long tempo = System.currentTimeMillis() - inicio;
            log.info("Cache MISS - id={} - {} ms (expirado pelo TTL)", id, tempo);
            return Optional.empty();
        }

        // Item válido: conta HIT e atualiza o último acesso (para o LRU).
        entrada.registrarAcesso(Instant.now());
        hits.incrementAndGet();
        long tempo = System.currentTimeMillis() - inicio;
        log.info("Cache HIT - id={} - {} ms", id, tempo);
        return Optional.of(entrada.getConteudo());
    }


     // Guarda (ou atualiza) um conteúdo no cache.
     // Se o cache já estiver cheio, remove o item com o "último acesso" mais antigo (política LRU) e conta-se uma eviction
     // item -> conteúdo que acabou de ser buscado na fonte (banco).
    public void armazenar(ConteudoCacheItem item) {
        if (item == null || item.getId() == null) {
            return;
        }
        Long id = item.getId();

        // Se o item já está no cache, apenas substitui (sem contar eviction).
        EntradaCache anterior = cache.put(id, new EntradaCache(item, Instant.now()));
        if (anterior != null) {
            return;
        }

        // Item novo: se estourou o limite, aplica o LRU.
        while (cache.size() > maxItens) {
            Long idRemovido = idMenosUsadoRecentemente();
            if (idRemovido == null) {
                break;
            }
            cache.remove(idRemovido);
            evictions.incrementAndGet();
            log.info("Cache EVICTION - id={} removido por limite (LRU) - tamanho={}", idRemovido, cache.size());
        }
    }

    // Invalidar a entrada de um conteúdo no cache
    public boolean invalidar(Long id) {
        boolean removido = cache.remove(id) != null;
        if (removido) {
            log.info("Cache INVALIDADO - id={}", id);
        }
        return removido;
    }

    // Limpar inteiramente o cache
    public void limpar() {
        int quantidade = cache.size();
        cache.clear();
        log.info("Cache LIMPO - {} itens removidos", quantidade);
    }

    // Metodo para montar o endpoint de estatísticas
    public Map<String, Object> estatisticas() {
        long totalHits = hits.get();
        long totalMisses = misses.get();
        long totalConsultas = totalHits + totalMisses;

        // Evita divisão por zero quando ainda não houve nenhuma consulta.
        double hitRatio = totalConsultas == 0 ? 0.0 : (double) totalHits / totalConsultas;

        Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("tamanhoAtual", cache.size());
        stats.put("limite", maxItens);
        stats.put("hits", totalHits);
        stats.put("misses", totalMisses);
        stats.put("evictions", evictions.get());
        stats.put("hitRatio", hitRatio);
        return stats;
    }

    // Metodo para procurar o item com o "último acesso" mais antigo (usado para o LRU e sua política
    // de sair o item de maior tempo sem uso)
    private Long idMenosUsadoRecentemente() {
        final Long[] candidato = {null};
        final Instant[] ultimoAcessoCandidato = {null};

        cache.forEach((id, entrada) -> {
            Instant ultimoAcesso = entrada.getUltimoAcesso();
            // Se ainda não achou ninguém, ou achou um acesso mais antigo, este é o candidato.
            if (candidato[0] == null || ultimoAcesso.isBefore(ultimoAcessoCandidato[0])) {
                candidato[0] = id;
                ultimoAcessoCandidato[0] = ultimoAcesso;
            }
        });

        return candidato[0];
    }

    public int getMaxItens() {
        return maxItens;
    }

    public long getTtlSegundos() {
        return ttlSegundos;
    }
}
