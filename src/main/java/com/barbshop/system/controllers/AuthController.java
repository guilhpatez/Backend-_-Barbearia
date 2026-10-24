package com.barbshop.system.controllers;

import com.barbshop.system.cloudflare.CloudflareService;
import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.security.JwtService;
import com.barbshop.system.requests.LoginRequest;
import com.barbshop.system.responses.LoginResponse;
import com.barbshop.system.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mk_barb/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager gerenciadorAuth;
    private final JwtService serviceJwt;
    private final CloudflareService cloudflareService;
    private final HttpUtils httpUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest infoLogin, HttpServletRequest servletRequest) {
        //Validação do cloudflare
        cloudflareService.validarOuFalhar(infoLogin.cloudflareToken(), httpUtils.ipCliente(servletRequest));

        try {
            //fazendo a autenticação
            Authentication autenticacao = gerenciadorAuth.authenticate(
                    new UsernamePasswordAuthenticationToken(infoLogin.email(),infoLogin.senha())
            );

            //Retornando o usuario
            UsersEntity usuario = (UsersEntity) autenticacao.getPrincipal();
            String token = serviceJwt.gerarToken(usuario);

            return ResponseEntity.ok(new LoginResponse(token, usuario.getUserNome(), usuario.getRoles().name()));

        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Email ou senha inválidos"));//Gera um objeto tipo JSON como resposta do erro
        }
    }

}
