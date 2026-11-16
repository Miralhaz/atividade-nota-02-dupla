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
 * CACHE "LIGHT" (feito à mão, sem Caffeine/Redis/@Cacheable).
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
 * <p>Por que {@link ConcurrentHashMap} e não {@code HashMap}?
 * Porque a API atende requisições simultâneas (várias threads).
 * O {@code HashMap} comum pode corromper a estrutura interna do mapa quando
 * é usado por várias threads ao mesmo tempo. O {@code ConcurrentHashMap} é
 * seguro para leitura/escrita concorrente.
 */
@Component
public class ConteudoCache {

    private static final Logger log = LoggerFactory.getLogger(ConteudoCache.class);

    /**
     * Mapa principal do cache: id -> entrada.
     */
    private final Map<Long, EntradaCache> cache = new ConcurrentHashMap<>();

    /**
     * Limite máximo de itens no cache (vem do application.properties).
     */
    private final int maxItens;

    /**
     * Tempo de vida de cada item, em segundos (vem do application.properties).
     */
    private final long ttlSegundos;

    /**
     * Contadores das estatísticas. {@link AtomicLong} garante que o "conta + 1" seja feito
     * de forma atômica (sem perder contagem quando duas threads somam ao mesmo tempo).
     */
    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);

    /**
     * O {@code @Value} injeta o valor da propriedade do application.properties.
     * Exemplo: {@code app.cache.max-itens=5} -> injeta o 5 aqui.
     */
    public ConteudoCache(@Value("${app.cache.max-itens:5}") int maxItens,
                          @Value("${app.cache.ttl-segundos:60}") long ttlSegundos) {
        this.maxItens = maxItens;
        this.ttlSegundos = ttlSegundos;
        log.info("ConteudoCache iniciado - limite={} itens, TTL={}s", this.maxItens, this.ttlSegundos);
    }

    /**
     * Tenta buscar um conteúdo no cache.
     *
     * <p>Este método é o CORAÇÃO do cache. Ele:
     * <ol>
     *     <li>olha se o id existe no mapa (se não existir -> MISS);</li>
     *     <li>olha se o item expirou pelo TTL (se expirou -> remove e conta MISS);</li>
     *     <li>se estiver válido -> atualiza o "último acesso" e conta HIT.</li>
     * </ol>
     *
     * <p>O log de HIT/MISS (com o tempo gasto) é feito aqui, dentro do cache,
     * porque é aqui que sabemos se foi HIT ou MISS.
     *
     * @param id identificador do conteúdo procurado.
     * @return o conteúdo se deu HIT (ou Optional.empty() se deu MISS).
     */
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

    /**
     * Guarda (ou atualiza) um conteúdo no cache.
     *
     * <p>Se o cache já estiver cheio, removemos o item com o "último acesso" mais
     * antigo (política LRU) e contamos uma <b>eviction</b>.
     *
     * @param item conteúdo que acabou de ser buscado na fonte (banco).
     */
    public void armazenar(ConteudoCacheItem item) {
        if (item == null || item.getId() == null) {
            return;
        }
        Long id = item.getId();

        // Se o item já está no cache, apenas substituímos (sem contar eviction).
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

    /**
     * Invalida (remove) a entrada de um conteúdo do cache.
     * Usado no PUT e no DELETE.
     *
     * @return true se havia algo no cache para remover.
     */
    public boolean invalidar(Long id) {
        boolean removido = cache.remove(id) != null;
        if (removido) {
            log.info("Cache INVALIDADO - id={}", id);
        }
        return removido;
    }

    /**
     * Limpa todo o cache (endpoint DELETE /cache).
     */
    public void limpar() {
        int quantidade = cache.size();
        cache.clear();
        log.info("Cache LIMPO - {} itens removidos", quantidade);
    }

    /**
     * Monta as estatísticas do cache para o endpoint GET /cache/estatisticas.
     *
     * @return um Map com os números (tamanho, limite, hits, misses, evictions, hitRatio).
     */
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

    /**
     * Procura o item cujo "último acesso" é o mais antigo (o menos usado recentemente).
     *
     * <p>É aqui que está o LRU: comparamos o instante de "último acesso" de cada
     * entrada e devolvemos o id da entrada mais antiga.
     *
     * @return o id do item a ser removido, ou null se não houver nada para remover.
     */
    private Long idMenosUsadoRecentemente() {
        final Long[] candidato = {null};
        final Instant[] ultimoAcessoCandidato = {null};

        cache.forEach((id, entrada) -> {
            Instant ultimoAcesso = entrada.getUltimoAcesso();
            // Se ainda não achamos ninguém, ou achamos um acesso mais antigo, este é o candidato.
            if (candidato[0] == null || ultimoAcesso.isBefore(ultimoAcessoCandidato[0])) {
                candidato[0] = id;
                ultimoAcessoCandidato[0] = ultimoAcesso;
            }
        });

        return candidato[0];
    }

    /**
     * @return o limite configurado (usado também nos testes).
     */
    public int getMaxItens() {
        return maxItens;
    }

    /**
     * @return o TTL configurado em segundos (usado também nos testes).
     */
    public long getTtlSegundos() {
        return ttlSegundos;
    }
}
