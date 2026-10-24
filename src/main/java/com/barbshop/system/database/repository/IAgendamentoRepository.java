package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.AgendamentoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import com.barbshop.system.enums.StatusPagamento;

@Repository
public interface IAgendamentoRepository extends JpaRepository<AgendamentoEntity, Integer>
{
    Page<AgendamentoEntity> findAllByEmailCliente(String email, Pageable pagina);

    //Query para a regra de negócio em que os agendamentos de um dia já passado, serão excluídos
    @Query("""
    select distinct agendamento
    from agendamento agendamento
    left join fetch agendamento.associacoesHorario associacao
    left join fetch associacao.horario
    where agendamento.data_horario_corte < :limite
    """)
    List<AgendamentoEntity> buscarAgendamentosVencidos(@Param("limite") LocalDateTime hoje);

    //Query para buscar o id order do mercado pago.
    Optional<AgendamentoEntity> findByIdPedidoMercadoPago(String idPedidoMercadoPago);

    Optional<AgendamentoEntity> findByTokenPagamentoPublico(String tokenPagamentoPublico);

    // Alterado pelo Codex: bloqueia uma reserva enquanto a cobrança PIX é preparada.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agendamento from agendamento agendamento where agendamento.id_agendamento = :id")
    Optional<AgendamentoEntity> buscarPorIdParaPagamento(@Param("id") Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agendamento from agendamento agendamento where agendamento.tokenPagamentoPublico = :token")
    Optional<AgendamentoEntity> buscarPorTokenPagamentoPublicoParaAtualizacao(@Param("token") String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agendamento from agendamento agendamento where agendamento.idPedidoMercadoPago = :idPedido")
    Optional<AgendamentoEntity> buscarPorIdPedidoParaAtualizacao(@Param("idPedido") String idPedido);

    // Alterado pelo Codex: encontra reservas PIX que venceram e podem devolver os horários à agenda.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    select agendamento
    from agendamento agendamento
    where agendamento.status = :status
      and agendamento.pagamentoExpiraEm <= :agora
    """)
    List<AgendamentoEntity> buscarPagamentosPixVencidos(
            @Param("status") StatusPagamento status,
            @Param("agora") LocalDateTime agora
    );
}
