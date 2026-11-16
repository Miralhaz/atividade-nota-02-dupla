package sptech.school.atividade_nota_02_dupla.mapper;

import sptech.school.atividade_nota_02_dupla.dto.ConteudoRequest;
import sptech.school.atividade_nota_02_dupla.dto.ConteudoResponse;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;

/**
 * MAPPER: faz as conversões entre Entidade (banco) e DTO (API).
 *
 * <p>Na nossa arquitetura o Service devolve ENTIDADE e a Controller devolve DTO.
 * Portanto:
 * <ul>
 *     <li>{@link #paraEntidade(ConteudoRequest)} -> usado no POST/PUT (DTO -> Entidade)</li>
 *     <li>{@link #paraResponse(Conteudo)} -> usado no GET (Entidade -> DTO)</li>
 * </ul>
 *
 * <p>O Mapper é uma classe "burra" e estática de propósito: assim não precisa
 * virar bean do Spring nem receber injeção de dependência.
 */
public final class ConteudoMapper {

    private ConteudoMapper() {
        // Construtor privado: ninguém cria instância do Mapper (classe utilitária).
    }

    /**
     * Converte o que chegou na requisição (DTO) para uma Entidade pronta para salvar.
     */
    public static Conteudo paraEntidade(ConteudoRequest request) {
        if (request == null) {
            return null;
        }
        return new Conteudo(request.getTitulo(), request.getCorpo(), request.getAutor());
    }

    /**
     * Converte a Entidade do banco para o DTO que vai virar JSON na resposta.
     */
    public static ConteudoResponse paraResponse(Conteudo conteudo) {
        if (conteudo == null) {
            return null;
        }
        return new ConteudoResponse(
                conteudo.getId(),
                conteudo.getTitulo(),
                conteudo.getCorpo(),
                conteudo.getAutor(),
                conteudo.getAtualizadoEm());
    }
}
