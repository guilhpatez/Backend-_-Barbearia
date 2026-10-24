package com.barbshop.system.payment;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.responses.PagamentoStatusResponse;
import com.mercadopago.client.order.OrderClient;
import com.mercadopago.client.order.OrderCreateRequest;
import com.mercadopago.client.order.OrderPayerRequest;
import com.mercadopago.client.order.OrderPaymentMethodRequest;
import com.mercadopago.client.order.OrderPaymentRequest;
import com.mercadopago.client.order.OrderTransactionRequest;
import com.mercadopago.core.MPRequestOptions;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.order.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PixPagamentoPreparacaoService preparacaoService;

    // Alterado pelo Codex: rota pública só funciona com token aleatório da reserva, nunca com ID sequencial.
    public PixPaymentResponse gerarCobrancaPagamentoPixPublico(String tokenPagamento)
            throws NotFoundException {
        return gerarCobranca(preparacaoService.prepararPagamentoPublico(tokenPagamento));
    }

    // Alterado pelo Codex: usuário autenticado só pode cobrar PIX do próprio agendamento.
    public PixPaymentResponse gerarCobrancaPagamentoPixDoUsuario(Integer idAgendamento, UsersEntity usuario)
            throws NotFoundException {
        return gerarCobranca(preparacaoService.prepararPagamentoDoUsuario(idAgendamento, usuario));
    }

    private PixPaymentResponse gerarCobranca(PixCobrancaContexto contexto) throws NotFoundException {
        if (contexto.pixExistente() != null) {
            return contexto.pixExistente();
        }

        String valorAgendamento = contexto.valor().toPlainString();
        long minutosRestantes = Math.max(
                1,
                Duration.between(LocalDateTime.now(), contexto.pagamentoExpiraEm()).toMinutes()
        );

        OrderPaymentMethodRequest metodoPagamento = OrderPaymentMethodRequest.builder()
                .id("pix")
                .type("bank_transfer")
                .build();

        OrderPaymentRequest pagamento = OrderPaymentRequest.builder()
                .amount(valorAgendamento)
                .paymentMethod(metodoPagamento)
                .expirationTime("PT" + minutosRestantes + "M")
                .build();

        OrderTransactionRequest transacao = OrderTransactionRequest.builder()
                .payments(List.of(pagamento))
                .build();

        OrderPayerRequest pagador = OrderPayerRequest.builder()
                .email(contexto.emailCliente())
                .build();

        OrderCreateRequest requisicao = OrderCreateRequest.builder()
                .type("online")
                .totalAmount(valorAgendamento)
                .externalReference("agendamento-" + contexto.idAgendamento())
                .processingMode("automatic")
                .payer(pagador)
                .transactions(transacao)
                .build();

        // Alterado pelo Codex: a chave já foi gravada antes da chamada externa e é reutilizada em retries.
        Map<String, String> headers = new HashMap<>();
        headers.put("x-idempotency-key", contexto.chaveIdempotencia());
        MPRequestOptions options = MPRequestOptions.builder().customHeaders(headers).build();

        try {
            Order order = new OrderClient().create(requisicao, options);
            var infoPagamento = order.getTransactions().getPayments().get(0);
            var metodo = infoPagamento.getPaymentMethod();

            return preparacaoService.registrarCobranca(
                    contexto.idAgendamento(),
                    order.getId(),
                    metodo.getQrCode(),
                    metodo.getQrCodeBase64(),
                    metodo.getTicketUrl()
            );
        } catch (MPApiException | MPException e) {
            throw new IllegalStateException("Não foi possível gerar a cobrança PIX. Tente novamente.", e);
        }
    }

    public void confirmarPagamento(String idPedidoMercadoPago, String referenciaExterna) {
        preparacaoService.confirmarPagamento(idPedidoMercadoPago, referenciaExterna);
    }

    public PagamentoStatusResponse consultarStatusPublico(String tokenPagamento)
            throws NotFoundException {
        return preparacaoService.consultarStatusPublico(tokenPagamento);
    }

    public PagamentoStatusResponse consultarStatusDoUsuario(Integer idAgendamento, UsersEntity usuario)
            throws NotFoundException {
        return preparacaoService.consultarStatusDoUsuario(idAgendamento, usuario);
    }
}
