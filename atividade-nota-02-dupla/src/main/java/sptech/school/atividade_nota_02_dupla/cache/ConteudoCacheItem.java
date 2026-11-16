package sptech.school.atividade_nota_02_dupla.cache;

/**
 * Contrato mínimo que o cache precisa de um "conteúdo".
 *
 * <p>Por que não usar direto a entidade {@code Conteudo} dentro do cache?
 * Porque assim o cache NÃO fica acoplado ao JPA/banco: ele só precisa de
 * um id para usar como chave. É uma boa prática de desenho (o cache é
 * "light" e genérico). Se a entidade mudar, o cache não quebra.
 *
 * <p>A entidade {@code Conteudo} implementa esta interface.
 */
public interface ConteudoCacheItem {

    /**
     * @return o identificador do conteúdo (chave do Map no cache).
     */
    Long getId();
}
