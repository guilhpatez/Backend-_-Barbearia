package com.barbshop.system.controllers.UsersControllers.Agendamento;


import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.requests.AgendamentoRequest;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.AgendamentoService;
import com.barbshop.system.responses.AgendamentoCriadoResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/agendamento/create")
@AllArgsConstructor
@EnableMethodSecurity
public class MarcarAgendamentoUsuarioController {

    private final AgendamentoService service;

    @PreAuthorize("hasRole('USER_COMMOM')")
    //Enviar dados ao banco (salvar o corte) (POST)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoCriadoResponse saveCut(@Valid @RequestBody AgendamentoRequest dto,
                                              @AuthenticationPrincipal UsersEntity user)
            throws BadRequest, NotFoundException {
        return service.saveCut(dto,user);
    }


}
