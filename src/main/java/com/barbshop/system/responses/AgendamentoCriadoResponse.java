package com.barbshop.system.responses;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.enums.StatusPagamento;
import com.barbshop.system.enums.TipoPagamento;

import java.time.LocalDateTime;

// Alterado pelo Codex: devolve somente os dados necessários para o frontend iniciar o PIX.
public record AgendamentoCriadoResponse(
        Integer idAgendamento,
        TipoPagamento tipoPagamento,
        StatusPagamento statusPagamento,
        String tokenPagamentoPublico,
        LocalDateTime pagamentoExpiraEm
) {
    public static AgendamentoCriadoResponse from(AgendamentoEntity agendamento,
                                                 boolean incluirTokenPublico) {
        return new AgendamentoCriadoResponse(
                agendamento.getId_agendamento(),
                agendamento.getTipoPagamento(),
                agendamento.getStatus(),
                incluirTokenPublico ? agendamento.getTokenPagamentoPublico() : null,
                agendamento.getPagamentoExpiraEm()
        );
    }
}
