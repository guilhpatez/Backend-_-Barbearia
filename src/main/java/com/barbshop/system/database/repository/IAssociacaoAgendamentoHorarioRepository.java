package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.AssociacaoAgendamentoHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IAssociacaoAgendamentoHorarioRepository extends JpaRepository<AssociacaoAgendamentoHorarioEntity, Integer> {

    //Vendo os horários por agendamento
    @Query("""
    SELECT associacao
    FROM agendamento_horario associacao
    WHERE associacao.agendamento.id_agendamento = :idAgendamento
    """)
    List<AssociacaoAgendamentoHorarioEntity> findAllByAgendamentoId(
            @Param("idAgendamento") Integer idAgendamento
    );
}
