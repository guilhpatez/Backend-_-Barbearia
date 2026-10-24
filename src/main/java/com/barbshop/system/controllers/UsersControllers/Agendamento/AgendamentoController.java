package com.barbshop.system.controllers.UsersControllers.Agendamento;

import com.barbshop.system.database.entity.AgendamentoEntity;
import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.responses.AgendamentoResponse;
import com.barbshop.system.services.AgendamentoService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/agendamento/view")
@RequiredArgsConstructor
@EnableMethodSecurity
public class AgendamentoController {

    private final AgendamentoService service;

    @PreAuthorize("hasRole('USER_COMMOM')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<AgendamentoResponse> findAllSchedulingByEmail(@AuthenticationPrincipal UsersEntity auth, @PageableDefault(
            size = 10, page = 0
    ) Pageable pageable) throws NotFoundException
    {
        return service.findAllByEmail(auth.getUserEmail(), pageable);
    }


}