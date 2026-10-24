package com.barbshop.system.controllers.AdminControllers;

import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.AgendamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/auth/login/admin/deletebyid")
@RequiredArgsConstructor
public class DeletarAgendamentoAdmin {

    private final AgendamentoService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBeingAdmin( @PathVariable("id") Integer id) throws NotFoundException {
        service.deleteBeingAdmin(id);
    }

}
