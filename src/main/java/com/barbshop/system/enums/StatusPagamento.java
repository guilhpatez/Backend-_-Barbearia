package com.barbshop.system.enums;

public enum StatusPagamento {
    PAGO,
    PENDENTE,
    PAGAMENTO_NO_DIA,
    // Alterado pelo Codex: reserva PIX não paga libera os horários sem apagar o histórico.
    EXPIRADO
}
