package com.barbshop.system.controllers.UsersControllers.Agendamento;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.payment.PaymentService;
import com.barbshop.system.payment.PixPaymentResponse;
import com.barbshop.system.responses.PagamentoStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/pagamento")
@RequiredArgsConstructor
public class PagamentoUsuarioController {

    private final PaymentService paymentService;

    // Alterado pelo Codex: a autorização é conferida no serviço pelo dono do agendamento.
    @PostMapping("/{idAgendamento}/pix")
    @PreAuthorize("hasRole('USER_COMMOM')")
    public PixPaymentResponse gerarPix(@PathVariable Integer idAgendamento,
                                       @AuthenticationPrincipal UsersEntity usuario)
            throws NotFoundException {
        return paymentService.gerarCobrancaPagamentoPixDoUsuario(idAgendamento, usuario);
    }

    @GetMapping("/{idAgendamento}/status")
    @PreAuthorize("hasRole('USER_COMMOM')")
    public PagamentoStatusResponse consultarStatus(@PathVariable Integer idAgendamento,
                                                   @AuthenticationPrincipal UsersEntity usuario)
            throws NotFoundException {
        return paymentService.consultarStatusDoUsuario(idAgendamento, usuario);
    }
}
