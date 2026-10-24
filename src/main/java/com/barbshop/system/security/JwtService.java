package com.barbshop.system.security;

import com.barbshop.system.database.entity.UsersEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${JWT_SECRET}")
    private String codeSecret;

    @Value("${JWT_TIME_EXPIRATION}")
    private long tempoExpiricao;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(codeSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(UserDetails userDetails) {
        if (!(userDetails instanceof UsersEntity usuario)
                || usuario.getIdUser() == null) {
            throw new IllegalArgumentException(
                    "Usuário persistido necessário para emitir token."
            );
        }

        Date agora = new Date();

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("userId", usuario.getIdUser())
                .issuedAt(agora)
                .expiration(
                        new Date(agora.getTime() + tempoExpiricao)
                )
                .signWith(getSigningKey())
                .compact();
    }

    public boolean tokenValido(
            String token,
            UserDetails userDetails
    ) {
        if (!(userDetails instanceof UsersEntity usuario)
                || usuario.getIdUser() == null) {
            return false;
        }

        Claims claims = extrairClaims(token);

        Integer idDoToken =
                claims.get("userId", Integer.class);

        Date expiracao = claims.getExpiration();

        return usuario.getIdUser().equals(idDoToken)
                && usuario.getUsername().equals(claims.getSubject())
                && expiracao != null
                && expiracao.after(new Date());
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    private boolean tokenExpirado(String token) {
        return extrairClaims(token).getExpiration().before(new Date());
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
