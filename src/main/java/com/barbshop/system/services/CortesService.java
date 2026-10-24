package com.barbshop.system.services;

import com.barbshop.system.database.entity.CortesEntity;
import com.barbshop.system.database.repository.ICortesRepository;
import com.barbshop.system.requests.AlterarCortesRequest;
import com.barbshop.system.requests.CortesRequest;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.security.UserVerifySecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
public class CortesService {

    private final ICortesRepository  repository;
    private final UserVerifySecurity verifySecurity;

    //Funções para GET
    public Page<CortesEntity> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public CortesEntity findById(Integer id) throws NotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Corte não encontrado."));
    }

    //Função para POST
    @PreAuthorize("hasRole('USER_ADMIN')")
    public void create(CortesRequest cortesRequest) {
        //verificando se é admin
        verifySecurity.adminVerify();

        if (cortesRequest == null) {
            throw new BadRequest("Houve algum erro de envio.");
        }

        Duration duracaoCorte = converterHorario(cortesRequest.tempo());

        CortesEntity entity = CortesEntity.builder()
                .nome(cortesRequest.nome().trim())
                .preco(cortesRequest.preco())
                .tempo(duracaoCorte)
                .build();

        repository.save(entity);

    }

    //Função para PATCH
    @PreAuthorize("hasRole('USER_ADMIN')")
    public void change(Integer id, AlterarCortesRequest cortesRequest) throws NotFoundException, BadRequest {
        //Verficando se é usuário
        verifySecurity.adminVerify();

        CortesEntity entity = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Corte não encontrado."));

        if(cortesRequest == null)
            throw new BadRequest("Envio errado ou vazio!");

        if (cortesRequest.nome() == null && cortesRequest.preco() == null && cortesRequest.tempo() == null)
            throw new BadRequest("Informe ao menos um campo para alterar.");

        if(cortesRequest.nome() != null) {
            if (cortesRequest.nome().isBlank())
                throw new BadRequest("O nome do serviço não pode ficar vazio.");
            entity.setNome(cortesRequest.nome().trim());
        }


        if(cortesRequest.preco() != null)
            entity.setPreco(cortesRequest.preco());


        if (cortesRequest.tempo() != null)
            entity.setTempo(converterHorario(cortesRequest.tempo()));


        repository.save(entity);

    }

    //Funções para DELETE
    @PreAuthorize("hasRole('USER_ADMIN')")
    public void delete(Integer id) {
        //Verficando se é usuário
        verifySecurity.adminVerify();

        repository.deleteById(id);
    }

    //Métodos
    public Duration converterHorario(LocalTime tempo) {

        if (tempo == null || tempo.equals(LocalTime.MIDNIGHT)) {
            throw new BadRequest(
                    "A duração deve ser maior que zero."
            );
        }

        return Duration.ofNanos(tempo.toNanoOfDay());


    }
}
