package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.HorariosEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface IHorariosRepository extends JpaRepository<HorariosEntity, Integer> {


    //Encontrar horarios ocupados por mes
    @Query(
            value = """
        SELECT h.*
        FROM horarios_entity h
        WHERE YEAR(h.data) = :ano
          AND MONTH(h.data) = :mes
          AND EXISTS (
              SELECT 1 FROM agendamento_horario ah
              WHERE ah.horario_id = h.id
          )
        ORDER BY h.data, h.horario, h.id
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM horarios_entity h
        WHERE YEAR(h.data) = :ano
          AND MONTH(h.data) = :mes
          AND EXISTS (
              SELECT 1 FROM agendamento_horario ah
              WHERE ah.horario_id = h.id
          )
        """,
            nativeQuery = true
    )
    Page<HorariosEntity> findHorariosMarcadosPorMes(
            @Param("ano") int ano,
            @Param("mes") int mes,
            Pageable pageable
    );

    //Encontrar horários ocupados por dia
    @Query(
            value = """
        SELECT h.*
        FROM horarios_entity h
        WHERE h.data = :data
          AND EXISTS (
              SELECT 1 FROM agendamento_horario ah
              WHERE ah.horario_id = h.id
          )
        ORDER BY h.horario, h.id
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM horarios_entity h
        WHERE h.data = :data
          AND EXISTS (
              SELECT 1 FROM agendamento_horario ah
              WHERE ah.horario_id = h.id
          )
        """,
            nativeQuery = true
    )
    Page<HorariosEntity> findHorariosEmUsoPorDia(
            @Param("data") LocalDate data,
            Pageable pageable
    );

    List<HorariosEntity> findAllByDataAndHorarioIn(
            LocalDate data,
            Collection<LocalTime> horarios
    );
}
