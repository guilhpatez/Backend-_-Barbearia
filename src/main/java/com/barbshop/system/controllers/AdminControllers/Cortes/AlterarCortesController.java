package com.barbshop.system.controllers.AdminControllers.Cortes;

import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.requests.AlterarCortesRequest;
import com.barbshop.system.services.CortesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/admin/cortes/mudar")
@RequiredArgsConstructor
@EnableMethodSecurity
public class AlterarCortesController {

    private final CortesService service;

    @PreAuthorize("hasRole('USER_ADMIN')")
    @PatchMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public void change(@PathVariable("id") Integer id, @Valid @RequestBody AlterarCortesRequest dto) throws NotFoundException, BadRequest
    {
        service.change(id, dto);
    }

}
