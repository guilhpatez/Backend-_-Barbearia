package com.barbshop.system.payment;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.database.repository.IAgendamentoRepository;
import com.barbshop.system.enums.StatusPagamento;
import com.barbshop.system.enums.TipoPagamento;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.responses.PagamentoStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class PixPagamentoPreparacaoService {

    private final IAgendamentoRepository agendamentoRepository;

    // Alterado pelo Codex: esta transação termina antes da chamada externa, persistindo a chave idempotente.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PixCobrancaContexto prepararPagamentoPublico(String tokenPagamento)
            throws NotFoundException {
        AgendamentoEntity agendamento = agendamentoRepository
                .buscarPorTokenPagamentoPublicoParaAtualizacao(tokenPagamento)
                .orElseThrow(() -> new NotFoundException("Reserva PIX não encontrada."));

        return preparar(agendamento);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PixCobrancaContexto prepararPagamentoDoUsuario(Integer idAgendamento, UsersEntity usuario)
            throws NotFoundException {
        AgendamentoEntity agendamento = agendamentoRepository.buscarPorIdParaPagamento(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (!agendamento.getEmailCliente().equalsIgnoreCase(usuario.getUserEmail())) {
            throw new AccessDeniedException("Você não pode gerar PIX para este agendamento.");
        }

        return preparar(agendamento);
    }

    private PixCobrancaContexto preparar(AgendamentoEntity agendamento) {
        if (agendamento.getTipoPagamento() != TipoPagamento.ONLINE) {
            throw new BadRequest("Este agendamento não utiliza pagamento PIX.");
        }

        if (agendamento.getStatus() != StatusPagamento.PENDENTE) {
            throw new BadRequest("O pagamento deste agendamento não está pendente.");
        }

        if (agendamento.getPagamentoExpiraEm() == null
                || !agendamento.getPagamentoExpiraEm().isAfter(LocalDateTime.now())) {
            throw new BadRequest("O prazo para este pagamento PIX expirou.");
        }

        if (agendamento.getIdPedidoMercadoPago() != null
                && agendamento.getCodigoPix() != null
                && agendamento.getQrCodePixBase64() != null) {
            return contexto(agendamento, respostaPix(agendamento));
        }

        if (agendamento.getChaveIdempotenciaPix() == null) {
            agendamento.setChaveIdempotenciaPix(UUID.randomUUID().toString());
            agendamentoRepository.saveAndFlush(agendamento);
        }

        return contexto(agendamento, null);
    }

    // Alterado pelo Codex: grava a mesma cobrança retornada em todas as tentativas concorrentes.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PixPaymentResponse registrarCobranca(Integer idAgendamento,
                                                String idPedidoMercadoPago,
                                                String codigoPix,
                                                String qrCodePixBase64,
                                                String ticketUrl) throws NotFoundException {
        AgendamentoEntity agendamento = agendamentoRepository.buscarPorIdParaPagamento(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (agendamento.getStatus() != StatusPagamento.PENDENTE
                || agendamento.getPagamentoExpiraEm() == null
                || !agendamento.getPagamentoExpiraEm().isAfter(LocalDateTime.now())) {
            throw new BadRequest("O prazo para este pagamento PIX expirou.");
        }

        if (agendamento.getIdPedidoMercadoPago() != null) {
            if (!agendamento.getIdPedidoMercadoPago().equals(idPedidoMercadoPago)) {
                throw new BadRequest("Já existe uma cobrança PIX para este agendamento.");
            }
            return respostaPix(agendamento);
        }

        agendamento.setIdPedidoMercadoPago(idPedidoMercadoPago);
        agendamento.setCodigoPix(codigoPix);
        agendamento.setQrCodePixBase64(qrCodePixBase64);
        agendamento.setTicketUrlPix(ticketUrl);
        agendamentoRepository.save(agendamento);

        return respostaPix(agendamento);
    }

    @Transactional
    public void confirmarPagamento(String idPedidoMercadoPago, String referenciaExterna) {
        agendamentoRepository.buscarPorIdPedidoParaAtualizacao(idPedidoMercadoPago)
                .ifPresent(agendamento -> {
                    String referenciaEsperada = "agendamento-" + agendamento.getId_agendamento();

                    if (!referenciaEsperada.equals(referenciaExterna)) {
                        throw new BadRequest("A referência do pagamento não corresponde ao agendamento.");
                    }

                    if (agendamento.getStatus() != StatusPagamento.PENDENTE) {
                        return;
                    }

                    if (agendamento.getPagamentoExpiraEm() == null
                            || !agendamento.getPagamentoExpiraEm().isAfter(LocalDateTime.now())) {
                        return;
                    }

                    agendamento.setStatus(StatusPagamento.PAGO);
                    agendamentoRepository.save(agendamento);
                });
    }

    @Transactional(readOnly = true)
    public PagamentoStatusResponse consultarStatusPublico(String tokenPagamento)
            throws NotFoundException {
        AgendamentoEntity agendamento = agendamentoRepository.findByTokenPagamentoPublico(tokenPagamento)
                .orElseThrow(() -> new NotFoundException("Reserva PIX não encontrada."));
        return respostaStatus(agendamento);
    }

    @Transactional(readOnly = true)
    public PagamentoStatusResponse consultarStatusDoUsuario(Integer idAgendamento, UsersEntity usuario)
            throws NotFoundException {
        AgendamentoEntity agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        if (!agendamento.getEmailCliente().equalsIgnoreCase(usuario.getUserEmail())) {
            throw new AccessDeniedException("Você não pode consultar este pagamento.");
        }

        return respostaStatus(agendamento);
    }

    private PixCobrancaContexto contexto(AgendamentoEntity agendamento, PixPaymentResponse pixExistente) {
        return new PixCobrancaContexto(
                agendamento.getId_agendamento(),
                agendamento.getEmailCliente(),
                agendamento.getPrecoCobrado(),
                agendamento.getChaveIdempotenciaPix(),
                agendamento.getPagamentoExpiraEm(),
                pixExistente
        );
    }

    private PixPaymentResponse respostaPix(AgendamentoEntity agendamento) {
        return new PixPaymentResponse(
                agendamento.getCodigoPix(),
                agendamento.getQrCodePixBase64(),
                agendamento.getTicketUrlPix(),
                agendamento.getPagamentoExpiraEm()
        );
    }

    private PagamentoStatusResponse respostaStatus(AgendamentoEntity agendamento) {
        return new PagamentoStatusResponse(
                agendamento.getStatus(),
                agendamento.getPagamentoExpiraEm()
        );
    }
}
