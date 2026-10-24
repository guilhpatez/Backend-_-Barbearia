package com.barbshop.system.responses;

import com.barbshop.system.enums.StatusPagamento;

import java.time.LocalDateTime;

// Alterado pelo Codex: permite ao frontend acompanhar uma cobrança sem expor o agendamento inteiro.
public record PagamentoStatusResponse(StatusPagamento statusPagamento,
                                      LocalDateTime pagamentoExpiraEm) {
}
