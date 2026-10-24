package com.barbshop.system.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UsersRequest (
        @NotNull
        @NotBlank(message = "e-mail obrigatório.")
        @Email(message = "e-mail inválido.")
        @Size(max = 60)
        String email,

        @NotNull
        @Size(min = 6, max = 25)
        @NotBlank(message = "Senha é obrigatória.")
        String senha,

        @NotNull
        @NotBlank
        @Size(max = 25)
        String numTel,

        @NotNull
        @NotBlank
        String nome,

        String cloudflareToken
) {


}
