package com.barbshop.system.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotNull
        @NotBlank(message = "E-mail obrigatório.")
        @Email(message = "e-mail inválido.")
        @Size(max = 60)
        String email,

        @Size(min = 6, max = 25)
        @NotBlank(message = "Senha é obrigatória.")
        String senha,

        //Validação do cloudflare
        String cloudflareToken
) {
}
