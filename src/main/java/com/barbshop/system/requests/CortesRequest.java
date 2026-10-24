package com.barbshop.system.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalTime;

@Builder
public record CortesRequest(

    @NotBlank
    @Size(max = 100)
    String nome,

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    BigDecimal preco,

    @NotNull
    LocalTime tempo
    ){
}
