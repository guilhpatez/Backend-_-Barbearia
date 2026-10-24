package com.barbshop.system.requests;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReagendamentoRequest (@Future @NotNull LocalDateTime novoHorario) {
}
