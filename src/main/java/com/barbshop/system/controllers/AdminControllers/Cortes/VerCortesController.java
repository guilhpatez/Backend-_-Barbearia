package com.barbshop.system.controllers.AdminControllers.Cortes;

import com.barbshop.system.database.entity.CortesEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.CortesService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mk_barb/auth/login/admin/cortes/view")
@EnableMethodSecurity
public class VerCortesController {

    private final CortesService service;

    @PreAuthorize("hasRole('USER_ADMIN')")
    @GetMapping("/todos_tipos_de_cortes")
    @ResponseStatus(HttpStatus.OK)
    public Page<CortesEntity> findAll(@PageableDefault(size = 10, page = 0) Pageable pageable ) {
        return service.findAll(pageable);
    }

    @PreAuthorize("hasRole('USER_ADMIN')")
    @GetMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CortesEntity findById(@PathVariable("id") Integer id) throws NotFoundException {
        return service.findById(id);
    }

}
