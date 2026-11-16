package sptech.school.atividade_nota_02_dupla.dto;

/**
 * DTO com as ESTATÍSTICAS do cache, retornado por GET /cache/estatisticas.
 */
public class EstatisticasCacheResponse {

    private int tamanhoAtual;
    private int limite;
    private long hits;
    private long misses;
    private long evictions;
    private double hitRatio;

    public EstatisticasCacheResponse() {
    }

    public EstatisticasCacheResponse(int tamanhoAtual, int limite, long hits, long misses, long evictions, double hitRatio) {
        this.tamanhoAtual = tamanhoAtual;
        this.limite = limite;
        this.hits = hits;
        this.misses = misses;
        this.evictions = evictions;
        this.hitRatio = hitRatio;
    }

    public int getTamanhoAtual() {
        return tamanhoAtual;
    }

    public void setTamanhoAtual(int tamanhoAtual) {
        this.tamanhoAtual = tamanhoAtual;
    }

    public int getLimite() {
        return limite;
    }

    public void setLimite(int limite) {
        this.limite = limite;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public long getEvictions() {
        return evictions;
    }

    public void setEvictions(long evictions) {
        this.evictions = evictions;
    }

    public double getHitRatio() {
        return hitRatio;
    }

    public void setHitRatio(double hitRatio) {
        this.hitRatio = hitRatio;
    }
}
