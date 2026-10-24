package com.barbshop.system.security;

import com.barbshop.system.ratelimiter.RateLimiterFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


//Classe de configuração da API
@Configuration
@EnableWebSecurity
// Alterado pelo Codex: garante a execução de @PreAuthorize nas rotas PIX autenticadas.
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimiterFilter rateLimiterFilter;

    @Value("${SITE_CORS}")
    private final String siteCorsAproved;

    //Configurando o CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        //Quem pode acessar API
        config.setAllowedOrigins(List.of("http://localhost:8080",
                "http://localhost:5500",
                siteCorsAproved));
        //
        config.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH", "DELETE", "OPTIONS"));
        // Alterado pelo Codex: token público do PIX é enviado em cabeçalho, não em URL previsível.
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Booking-Token"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    //Aqui é onde está os filtro para as autorização do uso dos endpoints
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .csrf(csfr -> csfr.disable()) //Desativa a proteção por que a nossa API não usa cookies.
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //não guarda sessão
                //Aqui está a configurações de acesso as rotas.
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/mk_barb/auth/login/admin/**").hasRole("USER_ADMIN")
                        .requestMatchers("/mk_barb/auth/login/usuario/**").hasAnyRole("USER_ADMIN","USER_COMMOM")
                        .requestMatchers("/mk_barb/auth/login").permitAll()
                        .requestMatchers("/mk_barb/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimiterFilter, JwtAuthFilter.class)
                .build();
    }

    //codificador das senhas
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager gerenciadorAuth(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
