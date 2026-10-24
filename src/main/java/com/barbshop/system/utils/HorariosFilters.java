package com.barbshop.system.utils;

import com.barbshop.system.exception.BadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HorariosFilters {


    private static final LocalTime INICIO_EXPEDIENTE =
            LocalTime.of(9, 0);

    private static final LocalTime FIM_EXPEDIENTE =
            LocalTime.of(20, 0);

    private static final Duration INTERVALO =
            Duration.ofMinutes(30);

    public void filtroHorarioAgendemento(
            LocalDateTime horario
    ) {
        if (horario == null) {
            throw new BadRequest("O horário é obrigatório.");
        }

        if (!horario.isAfter(LocalDateTime.now())) {
            throw new BadRequest(
                    "O horário deve estar no futuro."
            );
        }

        LocalTime hora = horario.toLocalTime();

        if (hora.isBefore(INICIO_EXPEDIENTE)
                || !hora.isBefore(FIM_EXPEDIENTE)) {
            throw new BadRequest(
                    "O início deve estar entre 09:00 e 19:30."
            );
        }

        if (hora.getMinute() % 30 != 0
                || hora.getSecond() != 0
                || hora.getNano() != 0) {
            throw new BadRequest(
                    "Escolha um horário da grade de 30 minutos."
            );
        }
    }

    public List<LocalDateTime> gerarHorariosOcupados(
            LocalDateTime inicio,
            Duration duracao
    ) {
        filtroHorarioAgendemento(inicio);

        if (duracao == null
                || duracao.isZero()
                || duracao.isNegative()) {
            throw new BadRequest(
                    "A duração deve ser maior que zero."
            );
        }

        Duration tempoDisponivel = Duration.between(
                inicio.toLocalTime(),
                FIM_EXPEDIENTE
        );

        if (duracao.compareTo(tempoDisponivel) > 0) {
            throw new BadRequest(
                    "A duração ultrapassa o encerramento às 20:00."
            );
        }

        long blocos = duracao.dividedBy(INTERVALO);

        if (!duracao.equals(INTERVALO.multipliedBy(blocos))) {
            blocos++;
        }

        LocalDateTime fimReservado =
                inicio.plus(INTERVALO.multipliedBy(blocos));

        if (!fimReservado.toLocalDate().equals(inicio.toLocalDate())
                || fimReservado.toLocalTime()
                .isAfter(FIM_EXPEDIENTE)) {
            throw new BadRequest(
                    "A reserva ultrapassa o encerramento às 20:00."
            );
        }

        List<LocalDateTime> horarios = new ArrayList<>();

        for (long i = 0; i < blocos; i++) {
            horarios.add(
                    inicio.plus(INTERVALO.multipliedBy(i))
            );
        }

        return horarios;
    }



}
