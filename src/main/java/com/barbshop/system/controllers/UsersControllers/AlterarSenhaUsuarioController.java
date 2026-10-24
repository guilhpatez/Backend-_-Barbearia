package com.barbshop.system.controllers.UsersControllers;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.requests.PasswordChangeRequest;
import com.barbshop.system.services.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/mudar_senha")
@EnableMethodSecurity
@RequiredArgsConstructor
public class AlterarSenhaUsuarioController {

    private final UsersService service;

    @PostMapping
    @PreAuthorize("hasRole('USER_COMMOM')")
    public void mudarSenha(@Valid @RequestBody PasswordChangeRequest request, @AuthenticationPrincipal UsersEntity user)
            throws NotFoundException, BadRequest
    {
        service.changePassword(request, user.getUserEmail());
    }

}
