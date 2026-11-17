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

@Entity
@Table(name = "conteudos")
public class Conteudo implements ConteudoCacheItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String corpo;

    @Column(nullable = false, length = 120)
    private String autor;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public Conteudo() {

    }

    public Conteudo(String titulo, String corpo, String autor) {
        this.titulo = titulo;
        this.corpo = corpo;
        this.autor = autor;
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

    // @PrePersist -> JPA chama e é usado para colocar um valor ANTES do insert
    @PrePersist
    public void prePersist() {
        this.atualizadoEm = LocalDateTime.now();
    }

    // @PreUpdate -> JPA chama e é usado para colocar um valor ANTES do update (sempre manter atualizado).
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Conteudo{id=" + id
                + ", titulo='" + titulo + '\''
                + ", autor='" + autor + '\''
                + ", atualizadoEm=" + atualizadoEm + '}';
    }
}
