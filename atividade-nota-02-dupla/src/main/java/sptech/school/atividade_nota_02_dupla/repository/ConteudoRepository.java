package sptech.school.atividade_nota_02_dupla.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sptech.school.atividade_nota_02_dupla.entity.Conteudo;

@Repository
public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {
}
