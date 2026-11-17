package sptech.school.atividade_nota_02_dupla.service;

import sptech.school.atividade_nota_02_dupla.entity.Conteudo;


// Resultado da busca: carrega a entidade + informação se foi HIT ou MISS no cache.
public record BuscaResultado(Conteudo conteudo, boolean hit) {
}