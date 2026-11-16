package sptech.school.atividade_nota_02_dupla.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import sptech.school.atividade_nota_02_dupla.cache.ConteudoCacheItem;

import java.time.LocalDateTime;

/**
 * ENTIDADE (JPA) que representa a "fonte" dos dados: a tabela de conteúdos no banco.
 *
 * <p>Camadas da aplicação (padrão da disciplina):
 * <ul>
 *     <li>{@code entity} -> o que está no banco (a "fonte" lenta)</li>
 *     <li>{@code dto} -> o que trafega na API (JSON)</li>
 *     <li>{@code service} -> regra de negócio + uso do cache</li>
 *     <li>{@code controller} -> endpoints HTTP + conversão Entidade -> DTO (Mapper)</li>
 * </ul>
 *
 * <p>Repare que esta classe não tem nada de cache: o cache é responsabilidade
 * do {@code ConteudoCache}, uma classe separada. Isso deixa o código mais fácil de entender.
 */
@Entity
@Table(name = "conteudos")
public class Conteudo implements ConteudoCacheItem {

    /**
     * Chave primária gerada pelo banco (identity).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Título do conteúdo.
     */
    @Column(nullable = false, length = 200)
    private String titulo;

    /**
     * Corpo/texto do conteúdo. Guardamos como TEXT porque pode ser grande.
     */
    @Column(columnDefinition = "TEXT")
    private String corpo;

    /**
     * Autor do conteúdo.
     */
    @Column(nullable = false, length = 120)
    private String autor;

    /**
     * Momento da última atualização do conteúdo.
     * Preenchido pelo próprio banco (@PreUpdate) e pelo application (@PrePersist).
     */
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public Conteudo() {
        // Construtor vazio é OBRIGATÓRIO no JPA (ele precisa instanciar a classe via reflection)
    }

    public Conteudo(String titulo, String corpo, String autor) {
        this.titulo = titulo;
        this.corpo = corpo;
        this.autor = autor;
    }

    // Getters e Setters
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

    /**
     * Chamado pelo JPA ANTES de inserir (INSERT). Aproveitamos para
     * preencher a data de criação/atualização.
     */
    @PrePersist
    public void prePersist() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * Chamado pelo JPA ANTES de atualizar (UPDATE). Assim o campo
     * "atualizadoEm" nunca fica desatualizado no banco.
     */
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /**
     * @return o "toString" legível, que ajuda bastante nos logs do console e no Postman.
     */
    @Override
    public String toString() {
        return "Conteudo{id=" + id
                + ", titulo='" + titulo + '\''
                + ", autor='" + autor + '\''
                + ", atualizadoEm=" + atualizadoEm + '}';
    }
}
