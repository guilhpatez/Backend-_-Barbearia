package com.barbshop.system.controllers.AdminControllers.Cortes;

import com.barbshop.system.requests.CortesRequest;
import com.barbshop.system.services.CortesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mk_barb/auth/login/admin/cortes/criar")
@EnableMethodSecurity
public class CriarCortesController {

    private final CortesService service;

    @PreAuthorize("hasRole('USER_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody CortesRequest dto) {
        service.create(dto);
    }


}
