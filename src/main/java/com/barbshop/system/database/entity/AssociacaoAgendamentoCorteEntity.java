/*
*   Essa é a tabela de associação entre o corte e o agendamento.
*   EXEMPLO..:
*
*    ID_AGENDAMENTO -   ID_CORTE
*           1              2
*           1              3
*
*   Nesse caso o mesmo agendamento (identificado pelo agendamento dele), está com dois tipos de corte diferente.
*
* */



package com.barbshop.system.database.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity(name = "associacao_agendamento_corte")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AssociacaoAgendamentoCorteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idAssociacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private AgendamentoEntity agendamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private CortesEntity tipoCorte;

}