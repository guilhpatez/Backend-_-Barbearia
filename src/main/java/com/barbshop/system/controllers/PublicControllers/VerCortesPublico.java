package com.barbshop.system.controllers.PublicControllers;

import com.barbshop.system.database.entity.CortesEntity;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.CortesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/mk_barb/public/cortes")
@RequiredArgsConstructor
public class VerCortesPublico {

    private final CortesService service;

    @GetMapping("/todos_tipos_de_cortes")
    @ResponseStatus(HttpStatus.OK)
    public Page<CortesEntity> findAll(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        return service.findAll(pageable);
    }


    @GetMapping("/byid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CortesEntity findById(@PathVariable("id") Integer id) throws NotFoundException {
        return service.findById(id);
    }


}
