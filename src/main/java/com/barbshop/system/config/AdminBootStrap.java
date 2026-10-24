package com.barbshop.system.config;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.database.repository.IUsersRepository;
import com.barbshop.system.enums.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminBootStrap {

    private final IUsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL}")
    private String email;

    @Value("${ADMIN_PASSWORD}")
    private String password;
    @Value("${ADMIN_NAME:Administrador MK}")
    private String nome;

    @Value("${ADMIN_PHONE:11999999999}")
    private String telefone;

    @Bean
    CommandLineRunner criarAdminInicial() {
        return args -> {
            if (email.isBlank() || password.isBlank()) return;

            if (usersRepository.findByUserEmail(email).isEmpty()) {
                usersRepository.save(
                        UsersEntity.builder()
                                .userNome(nome)
                                .userEmail(email)
                                .userSenha(passwordEncoder.encode(password))
                                .telefone_cliente(telefone)
                                .roles(Roles.USER_ADMIN)
                                .build()
                );
            }
        };
    }
}