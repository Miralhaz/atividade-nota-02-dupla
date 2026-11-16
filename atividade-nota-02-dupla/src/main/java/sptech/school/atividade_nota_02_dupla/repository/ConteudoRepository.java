package sptech.school.atividade_nota_02_dupla.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;

/**
 * Repositório de acesso ao banco (Spring Data JPA).
 *
 * <p>Ele já entrega o método {@code findById} que usamos como "fonte lenta".
 * Para deixar a lentidão visível, o {@code ConteudoService} roda um
 * {@code Thread.sleep(...)} antes de chamar este repositório.
 */
@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {
}
