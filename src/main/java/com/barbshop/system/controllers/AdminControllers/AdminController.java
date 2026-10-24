package com.barbshop.system.controllers.AdminControllers;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.responses.AgendamentoResponse;
import com.barbshop.system.responses.UsersResponse;
import com.barbshop.system.services.AgendamentoService;
import com.barbshop.system.services.UsersService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/mk_barb/auth/login/admin")
@RequiredArgsConstructor
@EnableMethodSecurity
public class AdminController {

    public final UsersService usersService;
    public final AgendamentoService agendamentoService;

    //Pega todos os usuários
    @PreAuthorize("hasRole('USER_ADMIN')")
    @GetMapping("/todos_usuarios")
    @ResponseStatus(HttpStatus.OK)
    public Page<UsersResponse> findAllUsers(@PageableDefault(size = 10, page = 0, sort = "user_nome") Pageable pageable)
            throws NotFoundException {
        return usersService.findAll(pageable);
    }

    //Pegar todos os horários marcados (GET)
    @PreAuthorize("hasRole('USER_ADMIN')")
    @GetMapping("/todos_agendamentos")
    @ResponseStatus(HttpStatus.OK)
    public Page<AgendamentoResponse> findAllScheduling(@PageableDefault(
            size = 10, page = 0
    ) Pageable pageable) throws NotFoundException {
        return agendamentoService.findAll(pageable);
    }


}
