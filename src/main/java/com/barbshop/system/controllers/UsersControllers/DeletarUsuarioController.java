package com.barbshop.system.controllers.UsersControllers;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.UsersService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/usuario/deletar")
@RequiredArgsConstructor
@EnableMethodSecurity
public class DeletarUsuarioController {

    private final UsersService service;

    @PreAuthorize("hasRole('USER_COMMOM')")
    @DeleteMapping()
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UsersEntity user, @RequestBody String senha) throws NotFoundException, BadRequest
    {
        service.delete(user.getUserEmail(), senha);
    }

}
