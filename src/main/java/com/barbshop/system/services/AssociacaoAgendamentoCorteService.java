package com.barbshop.system.services;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.database.entity.AssociacaoAgendamentoCorteEntity;
import com.barbshop.system.database.entity.CortesEntity;
import com.barbshop.system.database.repository.IAgendamentoRepository;
import com.barbshop.system.database.repository.IAssociacaoAgendamentoCorteRepository;
import com.barbshop.system.database.repository.ICortesRepository;
import com.barbshop.system.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssociacaoAgendamentoCorteService {

    private final IAssociacaoAgendamentoCorteRepository repository;
    private final ICortesRepository cortesRepository;
    private final IAgendamentoRepository agendamentoRepository;

    //salvando as associações dos agendamentos e dos cortes
    public void save(Integer idAgendamento, Integer idCorte) throws NotFoundException{
        //Carregando os cortes e o agendamento
        AgendamentoEntity agendamentoEntity = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));

        CortesEntity cortesEntity = cortesRepository.findById(idCorte)
                .orElseThrow(() -> new NotFoundException("Corte não encontrado."));

        //Colocando na entity para salvar
        AssociacaoAgendamentoCorteEntity entity = AssociacaoAgendamentoCorteEntity.builder()
                .agendamento(agendamentoEntity)
                .tipoCorte(cortesEntity)
                .build();

        //Salvando
        repository.save(entity);
    }

    //Pega quais são os cortes pelo ids deles
    public List<CortesEntity> buscarCortesPorIds(List<Integer> ids) throws NotFoundException {
        List<CortesEntity> cortes = cortesRepository.findAllById(ids);

        if (cortes.size() != ids.size()) {
            throw new NotFoundException("Um ou mais cortes não foram encontrados.");
        }

        return cortes;
    }

    //Pega todos os cortes do agendamento
    public List<CortesEntity> searchAllCortesByAgendamento(Integer idAgendamento) throws NotFoundException
    {

        List<AssociacaoAgendamentoCorteEntity> assoc = repository.findAllByAgendamento(idAgendamento)
                .orElseThrow(() -> new NotFoundException("Nenhum agendamento encontrado."));

        List<CortesEntity> listaCortes =  assoc
                .stream()
                .map(AssociacaoAgendamentoCorteEntity::getTipoCorte)
                .toList();


        return listaCortes;
    }

    //Vendo a duração do corte
    public Duration calcularDuracao(List<CortesEntity> cortes) {
        return cortes.stream()
                .map(CortesEntity::getTempo)
                .reduce(Duration.ZERO, Duration::plus);
    }


    //Calculo do total a ser pago
    public BigDecimal totalParaPagar(List<CortesEntity> listaCortes) throws NotFoundException {

        //Variaveis de valores e auxilio
        BigDecimal valorDouble = BigDecimal.valueOf(0.0);
        BigDecimal aux;

        //Percorre todos os cortes carregados, pega o valor de cada um e soma
        for(int i = 0; i < listaCortes.size(); i++)
        {
            aux = listaCortes.get(i).getPreco();
            valorDouble = valorDouble.add(aux);
        }

        //Retorna em BigDecimal o valor total
        return valorDouble;
    }

    //Lista de nomes dos cortes
    public List<String> listaCortesNome(Integer idAgendemento) throws NotFoundException {

        List<CortesEntity> listaCortes = this.searchAllCortesByAgendamento(idAgendemento);

        List<String> listaNomes = new ArrayList<>();

        for (int x = 0; x < listaCortes.size();x++) {
            listaNomes.add(String.valueOf((listaCortes.get(x).getNome())));
        }

        return listaNomes;
    }


}
