package com.barbshop.system.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "horarios_data_horario",
        columnNames = {"data", "horario"}
))
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HorariosEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(  nullable = false )
    private LocalDate data;

    @Column( nullable = false )
    private LocalTime horario;

}
