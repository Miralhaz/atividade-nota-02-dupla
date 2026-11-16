package sptech.school.atividade_nota_02_dupla.dto;

import java.time.LocalDateTime;

/**
 * DTO de SAÍDA (o que o cliente recebe no JSON da API).
 *
 * <p>Nunca devolvemos a entidade direto: o Controller converte
 * (via {@code ConteudoMapper}) para que o formato da API
 * não dependa do formato do banco.
 */
public class ConteudoResponse {

    private Long id;
    private String titulo;
    private String corpo;
    private String autor;
    private LocalDateTime atualizadoEm;

    public ConteudoResponse() {
    }

    public ConteudoResponse(Long id, String titulo, String corpo, String autor, LocalDateTime atualizadoEm) {
        this.id = id;
        this.titulo = titulo;
        this.corpo = corpo;
        this.autor = autor;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
