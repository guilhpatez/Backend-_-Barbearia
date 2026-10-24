package com.barbshop.system.controllers.UsersControllers.Agendamento;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.AgendamentoService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/agendamento/delete")
@RequiredArgsConstructor
@EnableMethodSecurity
public class ExcluirAgendamentoController {

    private final AgendamentoService service;

    @PreAuthorize("hasRole('USER_COMMOM')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id, @AuthenticationPrincipal UsersEntity users ) throws NotFoundException
    {
        service.delete(id, users);
    }

}
