package com.barbshop.system.controllers.PublicControllers;

import com.barbshop.system.cloudflare.CloudflareService;
import com.barbshop.system.requests.UsersRequest;
import com.barbshop.system.services.UsersService;
import com.barbshop.system.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/public/novo_usuario")
@RequiredArgsConstructor
public class NovoUsuarioController {

    private final UsersService service;
    private final CloudflareService cloudflareService;
    private final HttpUtils httpUtils;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@Valid @RequestBody UsersRequest dto, HttpServletRequest httpRequest){
        //Validação do cloudflare
        cloudflareService.validarOuFalhar(dto.cloudflareToken(), httpUtils.ipCliente(httpRequest));

        service.create(dto);
    }

}
