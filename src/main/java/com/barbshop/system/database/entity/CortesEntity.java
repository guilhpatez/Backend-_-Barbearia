/*
*  Essa é a entidade em que vai guardar o tipo de corte e seu valor.
*  EXEMPLO..:
*   ID   -  TIPO - VALOR
*   1    -  BARBA - 40,00
*
* */

package com.barbshop.system.database.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;

@Entity(name = "tipos_de_cortes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CortesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCortes;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Duration tempo;


}
