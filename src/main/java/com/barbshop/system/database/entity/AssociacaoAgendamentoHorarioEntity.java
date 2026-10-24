package com.barbshop.system.database.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity(name = "agendamento_horario")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AssociacaoAgendamentoHorarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( nullable = false)
    private AgendamentoEntity agendamento;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( nullable = false)
    private HorariosEntity horario;

}
