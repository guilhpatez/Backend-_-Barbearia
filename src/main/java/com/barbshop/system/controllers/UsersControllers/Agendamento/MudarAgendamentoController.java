package com.barbshop.system.controllers.UsersControllers.Agendamento;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.requests.ReagendamentoRequest;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/agendamento/change")
@RequiredArgsConstructor
@EnableMethodSecurity
public class MudarAgendamentoController {

    private final AgendamentoService service;

    @PreAuthorize("hasRole('USER_COMMOM')")
    @PatchMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void change(@PathVariable("id") Integer id, @AuthenticationPrincipal UsersEntity auth, @Valid @RequestBody ReagendamentoRequest reagendamentoRequest) throws NotFoundException, BadRequest
    {
        service.change(id, auth , reagendamentoRequest);
    }

}
