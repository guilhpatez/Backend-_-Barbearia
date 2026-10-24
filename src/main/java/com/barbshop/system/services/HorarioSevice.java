package com.barbshop.system.services;

import com.barbshop.system.database.entity.HorariosEntity;
import com.barbshop.system.database.repository.IHorariosRepository;
import com.barbshop.system.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioSevice {

    private final IHorariosRepository repository;

    public Page<HorariosEntity> acharHorariosEmUsoPorDia(Pageable pageable, LocalDate data) throws NotFoundException {
        Page<HorariosEntity> pagina = repository.findHorariosEmUsoPorDia(data, pageable);

        if(pagina.isEmpty())
            throw new NotFoundException("Nenhum horário marcado para esse dia.");

        return pagina;
    }

    public void save(HorariosEntity entity) {
        repository.save(entity);
    }

    //Busca os horarios ocupados
    public List<HorariosEntity> buscarHorariosOcupados(
            LocalDate data,
            Collection<LocalTime> horarios
    ) {
        return repository.findAllByDataAndHorarioIn(data, horarios);
    }

    public Page<HorariosEntity> acharHorariosEmUsoPorMes(int mes,Pageable pageable,int ano) throws NotFoundException
    {
        Page<HorariosEntity> pagina = repository.findHorariosMarcadosPorMes(ano,mes,pageable);

        if(pagina.isEmpty())
            throw new NotFoundException("Nenhum horário marcado para esse mÊs");

        return pagina;

    }

}