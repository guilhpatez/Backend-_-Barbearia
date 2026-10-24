package com.barbshop.system.requests;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record AlterarCortesRequest(@Size(max = 100) String nome,
                                   @Positive @Digits(integer = 8, fraction = 2) BigDecimal preco,
                                   LocalTime tempo
){
}
