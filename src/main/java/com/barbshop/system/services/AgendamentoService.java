package com.barbshop.system.services;

import com.barbshop.system.database.entity.*;
import com.barbshop.system.database.repository.IAgendamentoRepository;
import com.barbshop.system.database.repository.IAssociacaoAgendamentoHorarioRepository;
import com.barbshop.system.database.repository.IHorariosRepository;
import com.barbshop.system.utils.HorariosFilters;
import com.barbshop.system.requests.AgendamentoRequest;
import com.barbshop.system.requests.ReagendamentoRequest;
import com.barbshop.system.enums.StatusPagamento;
import com.barbshop.system.enums.TipoPagamento;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.responses.AgendamentoResponse;
import com.barbshop.system.responses.AgendamentoCriadoResponse;
import com.barbshop.system.security.UserVerifySecurity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final IAgendamentoRepository repository;
    private final AssociacaoAgendamentoCorteService serviceAssociacao;
    private final HorariosFilters horariosFilters;
    private final HorarioSevice serviceHorarios;
    private final IHorariosRepository horariosRepository;
    private final IAssociacaoAgendamentoHorarioRepository repositoryAssocHorarios;
    private final UserVerifySecurity verifySecurity;

    //Verifica se o agendamento pertence ao usuáro
    public boolean verifyIfSchedulingBelongsUser(Integer idAgendamento, UsersEntity users) throws NotFoundException
    {
        AgendamentoEntity agendamento = repository.findById(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        //Verificando se o email do agendamento é o mesmo do usuário logado.
        if(!agendamento.getEmailCliente().equalsIgnoreCase( users.getUserEmail()))
            return false;
        else
            return true;

    }
    //Salva novos horários para a troca
    private void salvarNovosHorarios(AgendamentoEntity agendamento, List<LocalDateTime> horariosParaReservar) {

        List<HorariosEntity> horarios = horariosParaReservar.stream()
                .map(dataHora -> HorariosEntity.builder()
                        .data(dataHora.toLocalDate())
                        .horario(dataHora.toLocalTime())
                        .build())
                .toList();

        try {
            horariosRepository.saveAllAndFlush(horarios);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequest("Um dos horários acabou de ser reservado.");
        }

        List<AssociacaoAgendamentoHorarioEntity> associacoes = horarios.stream()
                .map(horario -> AssociacaoAgendamentoHorarioEntity.builder()
                        .agendamento(agendamento)
                        .horario(horario)
                        .build())
                .toList();

        repositoryAssocHorarios.saveAll(associacoes);
    }

    //Libera os horários de agendamento no caso para troca
    private void liberarHorariosDoAgendamento(Integer idAgendamento) {
        List<AssociacaoAgendamentoHorarioEntity> associacoes =
                repositoryAssocHorarios.findAllByAgendamentoId(idAgendamento);

        List<HorariosEntity> horarios = associacoes.stream()
                .map(AssociacaoAgendamentoHorarioEntity::getHorario)
                .toList();

        // Primeiro remove as associações com as chaves estrangeiras.
        repositoryAssocHorarios.deleteAll(associacoes);
        repositoryAssocHorarios.flush();

        // Depois libera os horários reservados.
        horariosRepository.deleteAll(horarios);
        horariosRepository.flush();
    }

    //Funções para o uso em GET
    @SneakyThrows
    public Page<AgendamentoResponse> findAll(Pageable pageable) throws NotFoundException
    {
        //Verificar se é admin
        verifySecurity.adminVerify();

        Page<AgendamentoEntity> pageDeEntidades = repository.findAll(pageable);

        //Vendo se existe agendamento
        if(pageDeEntidades.isEmpty())
        {
            throw new NotFoundException("Não existe nenhum agendamento.");
        }


        return pageDeEntidades.map(entity -> {
            try {
                return AgendamentoResponse.filtroResponse(entity,serviceAssociacao.listaCortesNome(entity.getId_agendamento()));
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        });

    }
    

    @SneakyThrows
    public Page<AgendamentoResponse> findAllByEmail(String email, Pageable pageable) throws NotFoundException
    {
        //Verificar usuario
        verifySecurity.currentUserVerifyByEmail(email);

        Page<AgendamentoEntity> pagina = repository.findAllByEmailCliente(email,pageable);

        if (pagina.isEmpty())
        {
            throw new NotFoundException("Nenhum agendamento cadastrado nesse email");
        }

        return pagina.map(entity -> {
            try {
                return AgendamentoResponse.filtroResponse(entity,serviceAssociacao.listaCortesNome(entity.getId_agendamento()));
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    //Funções para uso em POST
    @Transactional(rollbackFor = NotFoundException.class)
    public AgendamentoCriadoResponse saveCut(AgendamentoRequest agendamentoRequest, UsersEntity users)
            throws BadRequest, NotFoundException {

        verifySecurity.currentUserVerifyByEmail(users.getUserEmail());

        // Alterado pelo Codex: somente o webhook pode alterar PIX pendente para pago.
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
                .nome_cliente(users.getUserNome())
                .telefone_cliente(users.getTelefone_cliente())
                .emailCliente(users.getUserEmail())
                .tipoPagamento(agendamentoRequest.tipoPagamento())
                .data_horario_corte(agendamentoRequest.data_horario_corte())
                .status(statusPagamento)
                .duracaoCorte(duracaoCorte)
                .precoCobrado(serviceAssociacao.totalParaPagar(listaCortes))
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
            horariosRepository.saveAllAndFlush(horarios);
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

        return AgendamentoCriadoResponse.from(novoAgendamento, false);
    }

    //Funções para uso em delete
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id, UsersEntity users) throws NotFoundException {
        verifySecurity.currentUserVerifyByEmail(users.getUserEmail());

        if (!verifyIfSchedulingBelongsUser(id, users)) {
            throw new AccessDeniedException(
                    "Acesso negado! O agendamento não pertence ao usuário."
            );
        }

        AgendamentoEntity agendamento = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Agendamento não encontrado."));

        liberarHorariosDoAgendamento(id);

        repository.delete(agendamento);
    }

    @PreAuthorize("hasRole('USER_ADMIN')")
    @Transactional(rollbackFor = Exception.class)
    public void deleteBeingAdmin(Integer id) throws NotFoundException {
        verifySecurity.adminVerify();

        AgendamentoEntity agendamento = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Agendamento não encontrado."));

        liberarHorariosDoAgendamento(id);

        repository.delete(agendamento);
    }


    //funções para PATCH
    @Transactional(rollbackFor = Exception.class)
    public void change(Integer id, UsersEntity users, ReagendamentoRequest request) throws NotFoundException, BadRequest {

        verifySecurity.currentUserVerifyByEmail(users.getUserEmail());

        if (!verifyIfSchedulingBelongsUser(id, users)) {
            throw new AccessDeniedException(
                    "Acesso negado! O agendamento não pertence ao usuário."
            );
        }

        if (request == null || request.novoHorario() == null) {
            throw new BadRequest("Não foi enviado o novo horário.");
        }

        AgendamentoEntity agendamento = repository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Agendamento não encontrado."));

        Duration duracao = agendamento.getDuracaoCorte();

        // Compatibilidade com agendamentos antigos sem duração salva.
        if (duracao == null) {
            List<CortesEntity> cortes = serviceAssociacao
                    .searchAllCortesByAgendamento(id);

            duracao = serviceAssociacao.calcularDuracao(cortes);
            agendamento.setDuracaoCorte(duracao);
        }

        List<LocalDateTime> novosHorarios = horariosFilters
                .gerarHorariosOcupados(request.novoHorario(), duracao);

        List<AssociacaoAgendamentoHorarioEntity> associacoesAntigas =
                repositoryAssocHorarios.findAllByAgendamentoId(id);

        Set<Integer> idsHorariosAtuais = associacoesAntigas.stream()
                .map(AssociacaoAgendamentoHorarioEntity::getHorario)
                .map(HorariosEntity::getId)
                .collect(Collectors.toSet());

        List<LocalTime> horasDesejadas = novosHorarios.stream()
                .map(LocalDateTime::toLocalTime)
                .toList();

        List<HorariosEntity> horariosOcupados =
                serviceHorarios.buscarHorariosOcupados(
                        request.novoHorario().toLocalDate(),
                        horasDesejadas
                );

        boolean existeConflito = horariosOcupados.stream()
                // Ignora os horários que já pertencem a este agendamento.
                .anyMatch(horario -> !idsHorariosAtuais.contains(horario.getId()));

        if (existeConflito) {
            throw new BadRequest("Um ou mais horários já estão ocupados.");
        }

        // Libera os blocos antigos, atualiza a data e reserva os novos.
        liberarHorariosDoAgendamento(id);

        agendamento.setData_horario_corte(request.novoHorario());
        repository.save(agendamento);

        salvarNovosHorarios(agendamento, novosHorarios);
    }

    //Regra de negócio para deletar os agendamento antigos.
    @Transactional
    public void deletarAgendamentosAntigos(){
        List<AgendamentoEntity> vencidos =
                repository.buscarAgendamentosVencidos(LocalDateTime.now());

        //Apagando cada agendamento vencido.
        for (AgendamentoEntity agendamento : vencidos) {
            liberarHorariosDoAgendamento(
                    agendamento.getId_agendamento()
            );

            repository.delete(agendamento);
        }
    }

    // Alterado pelo Codex: reserva PIX não paga expira e devolve seus horários automaticamente.
    @Scheduled(cron = "0 * * * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void expirarPagamentosPixPendentes() {
        List<AgendamentoEntity> vencidos = repository.buscarPagamentosPixVencidos(
                StatusPagamento.PENDENTE,
                LocalDateTime.now()
        );

        for (AgendamentoEntity agendamento : vencidos) {
            liberarHorariosDoAgendamento(agendamento.getId_agendamento());
            agendamento.setStatus(StatusPagamento.EXPIRADO);
            repository.save(agendamento);
        }
    }


}
