package com.barbshop.system.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Alterado pelo Codex: dados imutáveis usados entre a preparação transacional e a chamada ao provedor.
record PixCobrancaContexto(
        Integer idAgendamento,
        String emailCliente,
        BigDecimal valor,
        String chaveIdempotencia,
        LocalDateTime pagamentoExpiraEm,
        PixPaymentResponse pixExistente
) {
}
