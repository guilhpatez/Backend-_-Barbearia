package com.barbshop.system.database.entity;

import com.barbshop.system.enums.Roles;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity(name = "usuarios")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UsersEntity implements UserDetails {

    //Atribbutos da entidade
    //Id do corte
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idUser;

    @NotBlank
    @Column(nullable = false)
    private String userNome;


    @Column(nullable = false)
    private String userSenha;

    @Size(max = 60)
    @Column(unique = true, nullable = false)
    private String userEmail;

    @NonNull
    @NotBlank
    @Column(nullable = false)
    private String telefone_cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Roles roles;

    //Métodos do UserDetails do Spring
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + roles.name()));
    }

    @Override
    public @Nullable String getPassword() {
        return this.userSenha;
    }

    @Override
    public String getUsername() {
        return this.userEmail;
    }
}