package com.barbshop.system.ratelimiter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiterService {

    private final double requisicoes_por_seg;
    private final RateLimiter rateLimiterGlobal;

    //Criando um rate limiter por ip
    private final Cache<String, RateLimiter> limitersPorIp = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES) // remove IPs inativos, evita crescimento infinito
            .build();

    //Construtor
    public RateLimiterService(@Value("${RATE_LIMITER_PER_SEC_PER_IP:5.0}") double limiterPerSec
            ,@Value("${RATE_LIMITER_PER_SEC_GLOBAL:50.0}") double limiterPerSecGlobal)
    {
        this.requisicoes_por_seg = limiterPerSec;
        this.rateLimiterGlobal = RateLimiter.create(limiterPerSecGlobal);
    }

    public boolean permitirRequisicao(String ip) {



        //1ª barreira: nenhum IP individual pode usar toda essa capacidade sozinho
        try {
            RateLimiter limiterDoIp = limitersPorIp.get(ip, () -> RateLimiter.create(this.requisicoes_por_seg));
            return limiterDoIp.tryAcquire() && rateLimiterGlobal.tryAcquire();
        } catch (ExecutionException e) {
            return true; // fail-open: erro interno não deve bloquear o usuário
        }

        // 2ª barreira: capacidade total do servidor, não importa quantos IPs diferentes


    }

}
