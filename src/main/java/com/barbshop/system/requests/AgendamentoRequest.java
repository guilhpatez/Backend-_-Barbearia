package com.barbshop.system.requests;

import com.barbshop.system.enums.TipoPagamento;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AgendamentoRequest(


    //Atributos
    @Size(max = 100)
    @NotBlank
    @NotNull
    String nome_cliente,

    @NotBlank
    @Email
    @Size(max = 60)
    String email_cliente,

    @NotNull
    @Future
    LocalDateTime data_horario_corte,

    @NotBlank
    @NotNull
    @Size(max = 25)
    String telefone_cliente,


    @NotNull
    TipoPagamento tipoPagamento,
    


    @NotEmpty
    List<@NotNull @Positive Integer> listaCortes


){
}
