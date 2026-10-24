package com.barbshop.system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

import java.util.Arrays;

@Configuration
public class FireWallConfig {

    @Bean
    public HttpFirewall permissoesEspecificasFirewall() {

        StrictHttpFirewall strictHttpFirewall = new StrictHttpFirewall();


        //Métdos permitidos
        strictHttpFirewall.setAllowedHttpMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));

        // Bloqueia caracteres perigosos na URL (ex: pontos duplos .., barras invertidas \n, etc.)
        strictHttpFirewall.setAllowUrlEncodedSlash(false);
        strictHttpFirewall.setAllowBackSlash(false);
        strictHttpFirewall.setAllowSemicolon(false);

        return strictHttpFirewall;

    }


}

