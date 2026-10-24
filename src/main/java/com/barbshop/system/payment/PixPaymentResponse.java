package com.barbshop.system.payment;

import java.time.LocalDateTime;

public record PixPaymentResponse(
        String qrCode,
        String qrCodeBase64,
        String ticketUrl,
        // Alterado pelo Codex: o frontend exibe o tempo restante da reserva PIX.
        LocalDateTime pagamentoExpiraEm
) {
}
