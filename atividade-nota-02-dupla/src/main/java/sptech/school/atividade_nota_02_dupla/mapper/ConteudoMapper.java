package sptech.school.atividade_nota_02_dupla.mapper;

import sptech.school.atividade_nota_02_dupla.dto.ConteudoRequest;
import sptech.school.atividade_nota_02_dupla.dto.ConteudoResponse;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;

public final class ConteudoMapper {

    private ConteudoMapper() {

    }

    public static Conteudo paraEntidade(ConteudoRequest request) {
        if (request == null) {
            return null;
        }
        return new Conteudo(request.getTitulo(), request.getCorpo(), request.getAutor());
    }

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
