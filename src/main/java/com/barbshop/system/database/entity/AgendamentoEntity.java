package com.barbshop.system.database.entity;

import com.barbshop.system.enums.StatusPagamento;
import com.barbshop.system.enums.TipoPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "agendamento")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AgendamentoEntity {

    //Id do corte
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_agendamento;

    @Version
    @Column(nullable = false)
    private Long versao;

    //Atributos
    private String nome_cliente;

    @NotBlank
    @NonNull
    @Column( nullable = false)
    private String emailCliente;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPagamento tipoPagamento;

    // Alterado pelo Codex: a data é validada nos DTOs; a entidade precisa aceitar
    // atualizações do webhook mesmo depois do horário originalmente reservado.
    @Column(nullable = false)
    private LocalDateTime data_horario_corte;

    @NonNull
    @NotBlank
    @Column(nullable = false)
    private String telefone_cliente;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false)
    private StatusPagamento status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precoCobrado;


    private Duration duracaoCorte;

    // Alterado pelo Codex: dados persistidos para um PIX seguro, repetível e auditável.
    @Column(name = "id_pedido_mercado_pago", unique = true)
    private String idPedidoMercadoPago;

    @Column(name = "chave_idempotencia_pix", length = 36, unique = true)
    private String chaveIdempotenciaPix;

    @Column(name = "token_pagamento_publico", length = 36, unique = true)
    private String tokenPagamentoPublico;

    @Column(name = "pagamento_expira_em")
    private LocalDateTime pagamentoExpiraEm;

    @Column(name = "codigo_pix", length = 5000)
    private String codigoPix;

    @Column(name = "qr_code_pix_base64", length = 10000)
    private String qrCodePixBase64;

    @Column(name = "ticket_url_pix", length = 2048)
    private String ticketUrlPix;

    //Associação
    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssociacaoAgendamentoCorteEntity> associacoesCorte;

    @OneToMany(mappedBy = "agendamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssociacaoAgendamentoHorarioEntity> associacoesHorario = new ArrayList<>();

    public void adicionarHorario(HorariosEntity horario) {
        AssociacaoAgendamentoHorarioEntity item = AssociacaoAgendamentoHorarioEntity.builder()
                .agendamento(this)
                .horario(horario)
                .build();
        this.associacoesHorario.add(item);
    }


}
