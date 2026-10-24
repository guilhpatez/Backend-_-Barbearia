package com.barbshop.system.utils;

import com.barbshop.system.exception.BadRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestsFilter {


    //Filtro para senha
    public boolean isPasswordValid(String senha) throws BadRequest
    {
        //O mínimo de 6 caracteres e máximo de 25
        if(senha == null || senha.isBlank())
            throw new BadRequest("Senha vazia");
        else if (senha.length() < 6)
            throw new BadRequest("Tamanho da senha insuficiente. (Minímo de 6 caracteres)");
        else if (senha.length() >= 26)
            throw new BadRequest("Tamanho da senha maior que o permitido.");
        //Se pasasar pelas condições é válido
        return true;
    }



}
