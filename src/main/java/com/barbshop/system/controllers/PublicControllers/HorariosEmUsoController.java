package com.barbshop.system.controllers.PublicControllers;


import com.barbshop.system.database.entity.HorariosEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.HorarioSevice;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/mk_barb/public/horarios_ocupados")
@RequiredArgsConstructor
public class HorariosEmUsoController {

    private final HorarioSevice service;

    @GetMapping("/pormes_ano/{ano}/{mes}")
    @ResponseStatus(HttpStatus.OK)
    public Page<HorariosEntity> findAllSchudelingByMouth(@PageableDefault(size = 10, page = 0) Pageable pageable,
                                                         @PathVariable("mes") int mes, @PathVariable("ano") int ano)
        throws NotFoundException
    {
        return service.acharHorariosEmUsoPorMes(mes,pageable,ano);
    }

    @GetMapping("/pordia/{dia}")
    @ResponseStatus(HttpStatus.OK)
    public Page<HorariosEntity> findAllSchudelingByDay(@PathVariable("dia") LocalDate dia, @PageableDefault(size = 10, page = 0, sort = "data") Pageable pageable)
        throws NotFoundException
    {
        return  service.acharHorariosEmUsoPorDia(pageable,dia);
    }


}
