package sptech.school.atividade_nota_02_dupla.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de ENTRADA (o que o cliente envia no POST/PUT).
 *
 * <p>Por que existe um DTO de entrada e não usamos a entidade direto?
 * - Não deixa o cliente "massacrar" campos internos da entidade (como o id).
 * - Permite validar os dados ANTES de chegar no banco.
 */
public class ConteudoRequest {

    @NotBlank(message = "O titulo nao pode ser vazio")
    @Size(max = 200, message = "O titulo deve ter no maximo 200 caracteres")
    private String titulo;

    @Size(max = 10000, message = "O corpo deve ter no maximo 10000 caracteres")
    private String corpo;

    @NotBlank(message = "O autor nao pode ser vazio")
    @Size(max = 120, message = "O autor deve ter no maximo 120 caracteres")
    private String autor;

    public ConteudoRequest() {
    }

    public ConteudoRequest(String titulo, String corpo, String autor) {
        this.titulo = titulo;
        this.corpo = corpo;
        this.autor = autor;
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
}
