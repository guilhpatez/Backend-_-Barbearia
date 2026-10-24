package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.AssociacaoAgendamentoCorteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface IAssociacaoAgendamentoCorteRepository extends JpaRepository<AssociacaoAgendamentoCorteEntity, Integer>
{

    @Query(value= """
        SELECT * FROM associacao_agendamento_corte acc WHERE acc.agendamento_id_agendamento = :idAgendamento
        """, nativeQuery = true)
    Optional<List<AssociacaoAgendamentoCorteEntity>> findAllByAgendamento(@Param("idAgendamento") Integer idAgendamento);
}
