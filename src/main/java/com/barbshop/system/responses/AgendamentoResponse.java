package com.barbshop.system.responses;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.enums.TipoPagamento;
import com.barbshop.system.exception.NotFoundException;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record AgendamentoResponse(Integer id, String nome, String email, String nulTelefone,
                                  List<String> listacortes, TipoPagamento tipoPagamento, LocalDateTime dataHoraInicio,
                                  LocalDateTime dataHoraFim
                                ) {



    //Filtro para gerar um Response apartir da entity
    static public AgendamentoResponse filtroResponse(AgendamentoEntity entity,List<String> listacortes) throws NotFoundException {
        LocalDateTime dataHoraFim = null;
        if (entity.getDuracaoCorte() != null) {
            dataHoraFim = entity.getData_horario_corte()
                    .plus(entity.getDuracaoCorte());
        }

        return new AgendamentoResponse(
                entity.getId_agendamento(),
                entity.getNome_cliente(),
                entity.getEmailCliente(),
                entity.getTelefone_cliente(),
                listacortes,
                entity.getTipoPagamento(),
                entity.getData_horario_corte(),
                dataHoraFim
        );
    }


}
