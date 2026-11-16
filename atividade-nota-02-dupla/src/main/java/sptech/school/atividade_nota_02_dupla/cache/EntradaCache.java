package sptech.school.atividade_nota_02_dupla.cache;

import java.time.Instant;

/**
 * ENTRADA DO CACHE: guarda o conteúdo, quando ele foi armazenado e quando foi usado por último.
 *
 * <p>Esta classe é o "molde" do valor guardado no {@link java.util.Map}.
 * O {@code Map} guarda pares (chave -> valor):
 * <ul>
 *     <li>chave = {@link #id} (Long) -> identificador do conteúdo</li>
 *     <li>valor = esta classe -> o conteúdo + os metadados de tempo</li>
 * </ul>
 *
 * <p>Os campos de tempo são mutáveis (e o objeto é alterado dentro do cache),
 * por isso a classe NÃO é imutável (sem "final" nos campos).
 */
public class EntradaCache {

    /**
     * Conteúdo armazenado no cache.
     */
    private final ConteudoCacheItem conteudo;

    /**
     * Momento em que o item foi armazenado no cache (serve para calcular o TTL).
     */
    private final Instant armazenadoEm;

    /**
     * Momento do último acesso ao item (serve para a política LRU:
     * "Least Recently Used" = remove o que não é usado há mais tempo).
     */
    private Instant ultimoAcesso;

    public EntradaCache(ConteudoCacheItem conteudo, Instant armazenadoEm) {
        this.conteudo = conteudo;
        this.armazenadoEm = armazenadoEm;
        this.ultimoAcesso = armazenadoEm;
    }

    public ConteudoCacheItem getConteudo() {
        return conteudo;
    }

    public Instant getArmazenadoEm() {
        return armazenadoEm;
    }

    public Instant getUltimoAcesso() {
        return ultimoAcesso;
    }

    /**
     * Atualiza o marcador de "último acesso". Chamado a cada HIT,
     * porque um item acessado agora passa a ser o "mais recente".
     */
    public void registrarAcesso(Instant momento) {
        this.ultimoAcesso = momento;
    }

    /**
     * @param ttlSegundos tempo máximo de vida do item, em segundos.
     * @return true se o item já passou do TTL (ou seja, está "vencido"/expirado).
     */
    public boolean isExpirado(long ttlSegundos) {
        long idadeEmSegundos = java.time.Duration.between(armazenadoEm, Instant.now()).getSeconds();
        return idadeEmSegundos > ttlSegundos;
    }

    @Override
    public String toString() {
        return "EntradaCache{armazenadoEm=" + armazenadoEm
                + ", ultimoAcesso=" + ultimoAcesso
                + ", conteudo=" + conteudo + '}';
    }
}
