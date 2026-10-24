package com.barbshop.system.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record PasswordChangeRequest(
        @NotNull
        @Size(min = 6, max = 25)
        @NotBlank
        String newPassword,

        String cloudflareToken
) {
}
