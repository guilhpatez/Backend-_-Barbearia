package com.barbshop.system.controllers.AdminControllers.Cortes;


import com.barbshop.system.services.CortesService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/admin/cortes/deletar")
@RequiredArgsConstructor
@EnableMethodSecurity
public class DeletarCortesController {

    private final CortesService service;

    @PreAuthorize("hasRole('USER_ADMIN')")
    @DeleteMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id)
    {
        service.delete(id);
    }


}
