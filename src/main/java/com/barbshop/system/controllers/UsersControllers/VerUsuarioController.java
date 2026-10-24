package com.barbshop.system.controllers.UsersControllers;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.responses.UsersResponse;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.security.UserVerifySecurity;
import com.barbshop.system.services.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mk_barb/auth/login/usuario/view")
@EnableMethodSecurity
public class VerUsuarioController {

    private final UsersService service;
    private final UserVerifySecurity verifySecurity;

    @PreAuthorize("hasRole('USER_COMMOM')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public UsersResponse findByCurrentUser(@AuthenticationPrincipal UsersEntity user) throws NotFoundException {
        return service.findById(user.getIdUser());
    }

    @PreAuthorize("hasRole('USER_COMMOM')")
    @GetMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public UsersResponse findById(@PathVariable("id") Integer id) throws NotFoundException {
        return service.findById(id);
    }

    @PreAuthorize("hasRole('USER_COMMOM')")
    @GetMapping("/byemail/{email}")
    @ResponseStatus(HttpStatus.OK)
    public UsersResponse findByEmail(@PathVariable("email") String email) throws NotFoundException {

        return service.findByEmail(email);
    }

}

