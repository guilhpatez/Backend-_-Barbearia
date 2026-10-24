package com.barbshop.system.controllers.PublicControllers;

import com.barbshop.system.payment.PaymentService;
import com.mercadopago.client.order.OrderClient;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.exceptions.MPInvalidWebhookSignatureException;
import com.mercadopago.resources.order.Order;
import com.mercadopago.webhook.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mk_barb/public/pagamentos")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @Value("${MERCADO_PAGO_WEBHOOK_SECRET_KEY}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> receberNotificacao(
            @RequestParam(name = "data.id", required = false) String orderId,
            @RequestParam(name = "type", required = false) String type,
            @RequestHeader(name = "x-signature", required = false) String assinatura,
            @RequestHeader(name = "x-request-id", required = false) String requestId) {

        // Alterado pelo Codex: cabeçalhos obrigatórios são rejeitados antes da validação do provedor.
        if (orderId == null || assinatura == null || requestId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            WebhookSignatureValidator.validate(assinatura, requestId, orderId, webhookSecret);
        } catch (Exception e) {
            log.warn("Webhook rejeitado: assinatura inválida");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"order".equals(type) || orderId == null) {
            return ResponseEntity.ok().build(); // outro tipo de evento, ignora
        }

        try {
            OrderClient client = new OrderClient();
            Order order = client.get(orderId);

            if ("accredited".equals(order.getStatusDetail())) {
                // Alterado pelo Codex: confirmação só aceita a referência esperada e PIX ainda pendente.
                paymentService.confirmarPagamento(orderId, order.getExternalReference());
            }
        } catch (MPApiException | MPException e) {
            log.error("Erro ao consultar order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }
}
