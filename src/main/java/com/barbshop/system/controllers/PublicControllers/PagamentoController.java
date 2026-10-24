package com.barbshop.system.controllers.PublicControllers;

import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.payment.PaymentService;
import com.barbshop.system.payment.PixPaymentResponse;
import com.barbshop.system.responses.PagamentoStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/public/pagamento")
@RequiredArgsConstructor
public class PagamentoController {
    private final PaymentService pagamentoService;

    // Alterado pelo Codex: token de alta entropia substitui o ID sequencial em rota pública.
    @PostMapping("/pix")
    public PixPaymentResponse gerarPix(
            @RequestHeader("X-Booking-Token") String tokenPagamento
    ) throws NotFoundException {
        return pagamentoService.gerarCobrancaPagamentoPixPublico(tokenPagamento);
    }

    // Alterado pelo Codex: polling protegido pelo mesmo token, sem expor dados da agenda.
    @GetMapping("/status")
    public PagamentoStatusResponse consultarStatus(
            @RequestHeader("X-Booking-Token") String tokenPagamento
    ) throws NotFoundException {
        return pagamentoService.consultarStatusPublico(tokenPagamento);
    }

}
