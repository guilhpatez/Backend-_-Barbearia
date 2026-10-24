package com.barbshop.system.requests;

import com.barbshop.system.database.entity.HorariosEntity;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record HorariosRequest(LocalDate data, LocalTime hora ){

    public static HorariosRequest filtraEntity(HorariosEntity entity) {
        return new HorariosRequest(
                entity.getData(),
                entity.getHorario()
        );
    }

}
