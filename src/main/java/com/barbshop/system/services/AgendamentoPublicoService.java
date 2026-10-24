package com.barbshop.system.services;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.database.entity.AssociacaoAgendamentoHorarioEntity;
import com.barbshop.system.database.entity.CortesEntity;
import com.barbshop.system.database.entity.HorariosEntity;
import com.barbshop.system.database.repository.IAgendamentoRepository;
import com.barbshop.system.database.repository.IAssociacaoAgendamentoHorarioRepository;
import com.barbshop.system.database.repository.IHorariosRepository;
import com.barbshop.system.enums.StatusPagamento;
import com.barbshop.system.enums.TipoPagamento;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.utils.HorariosFilters;
import com.barbshop.system.requests.AgendamentoRequest;
import com.barbshop.system.responses.AgendamentoCriadoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgendamentoPublicoService {

    private final IAgendamentoRepository repository;
    private final HorarioSevice serviceHorarios;
    private final IHorariosRepository repositoryHorarios;
    private final IAssociacaoAgendamentoHorarioRepository repositoryAssocHorarios;
    private final AssociacaoAgendamentoCorteService serviceAssociacao;
    private final HorariosFilters horariosFilters;

    //Criar corte para o público
    @Transactional(rollbackFor = NotFoundException.class)
    public AgendamentoCriadoResponse saveCutForPublic(AgendamentoRequest agendamentoRequest)
            throws BadRequest, NotFoundException {

        // Alterado pelo Codex: o navegador jamais define se um PIX foi pago.
        boolean pagamentoOnline = agendamentoRequest.tipoPagamento() == TipoPagamento.ONLINE;
        StatusPagamento statusPagamento = pagamentoOnline
                ? StatusPagamento.PENDENTE
                : StatusPagamento.PAGAMENTO_NO_DIA;
        LocalDateTime pagamentoExpiraEm = pagamentoOnline
                ? LocalDateTime.now().plusMinutes(8)
                : null;


        //filtrando os horarios
        horariosFilters.filtroHorarioAgendemento(agendamentoRequest.data_horario_corte());

        //Lista de cortes e duração do corte completo pelo tempo de cada corte
        List<CortesEntity> listaCortes = serviceAssociacao.buscarCortesPorIds(agendamentoRequest.listaCortes());
        Duration duracaoCorte = serviceAssociacao.calcularDuracao(listaCortes);

        //Salvando o corte no banco de dados.
        AgendamentoEntity novoAgendamento = AgendamentoEntity.builder()
                .nome_cliente(agendamentoRequest.nome_cliente())
                .telefone_cliente(agendamentoRequest.telefone_cliente())
                .emailCliente(agendamentoRequest.email_cliente())
                .tipoPagamento(agendamentoRequest.tipoPagamento())
                .data_horario_corte(agendamentoRequest.data_horario_corte())
                .status(statusPagamento)
                .duracaoCorte(duracaoCorte)
                .precoCobrado(serviceAssociacao.totalParaPagar(listaCortes))
                .tokenPagamentoPublico(pagamentoOnline ? UUID.randomUUID().toString() : null)
                .pagamentoExpiraEm(pagamentoExpiraEm)
                .build();

        //Salvando o agendamento
        repository.save(novoAgendamento);

        //Salvando a associação de cortes
        for (Integer idCorte : agendamentoRequest.listaCortes()) {
            serviceAssociacao.save(novoAgendamento.getId_agendamento(), idCorte);
        }

        //calculando o tempo total do corte (caso o tempo dos corte ultrapasse os 30 minutos)
        List<LocalDateTime> horariosParaReservar =
                horariosFilters.gerarHorariosOcupados(
                        agendamentoRequest.data_horario_corte(), duracaoCorte
                );

        List<LocalTime> horas = horariosParaReservar.stream()
                .map(LocalDateTime::toLocalTime)
                .toList();

        List<HorariosEntity> ocupados =
                serviceHorarios.buscarHorariosOcupados(
                        agendamentoRequest.data_horario_corte().toLocalDate(), horas
                );

        if (!ocupados.isEmpty()) {
            throw new BadRequest("Um ou mais horários já estão ocupados.");
        }


        List<HorariosEntity> horarios = horariosParaReservar.stream()
                .map(dataHora -> HorariosEntity.builder()
                        .data(dataHora.toLocalDate())
                        .horario(dataHora.toLocalTime())
                        .build())
                .toList();

        try {
            repositoryHorarios.saveAllAndFlush(horarios);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequest("Um dos horários acabou de ser reservado.");
        }

        List<AssociacaoAgendamentoHorarioEntity> associacoes = horarios.stream()
                .map(horario -> AssociacaoAgendamentoHorarioEntity.builder()
                        .agendamento(novoAgendamento)
                        .horario(horario)
                        .build())
                .toList();

        repositoryAssocHorarios.saveAll(associacoes);

        // Alterado pelo Codex: o token só é devolvido no instante da criação pública.
        return AgendamentoCriadoResponse.from(novoAgendamento, pagamentoOnline);
    }


}
