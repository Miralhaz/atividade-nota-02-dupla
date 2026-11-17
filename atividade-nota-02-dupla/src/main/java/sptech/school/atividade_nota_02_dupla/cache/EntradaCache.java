package sptech.school.atividade_nota_02_dupla.cache;

import java.time.Instant;

public class EntradaCache {

    private final ConteudoCacheItem conteudo;

    // Usado para o TTL
    private final Instant armazenadoEm;

    // Usado para o LRU
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

    // Atualiza o marcador de "último acesso"
    public void registrarAcesso(Instant momento) {
        this.ultimoAcesso = momento;
    }

    // Metodo para verificar se o item já passou/expirou do TTL definido
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
