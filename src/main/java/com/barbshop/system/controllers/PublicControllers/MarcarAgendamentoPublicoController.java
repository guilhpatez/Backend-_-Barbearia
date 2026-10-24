package com.barbshop.system.controllers.PublicControllers;


import com.barbshop.system.requests.AgendamentoRequest;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.services.AgendamentoPublicoService;
import com.barbshop.system.responses.AgendamentoCriadoResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mk_barb/public/marcar")
@AllArgsConstructor
public class MarcarAgendamentoPublicoController {

    private final AgendamentoPublicoService service;

    //Enviar dados ao banco (salvar o corte) (POST)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AgendamentoCriadoResponse saveCutForPublic(@Valid @RequestBody AgendamentoRequest dto)
            throws BadRequest, NotFoundException
    {
       return service.saveCutForPublic(dto);
    }


}
