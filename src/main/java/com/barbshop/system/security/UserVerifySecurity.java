package com.barbshop.system.security;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.enums.Roles;
import com.barbshop.system.exception.BadAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;


@Component
public class UserVerifySecurity {

    //Pegando o atual usuário autenticado
    public UsersEntity getCurrentAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //Verificando se não está autenticado.
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadAuthentication("Autenticação necessária.");
        }

        return (UsersEntity) authentication.getPrincipal();
    }

    //Verificando se é
    public void adminVerify() throws AccessDeniedException  {
        UsersEntity user = this.getCurrentAuthUser();

        if ( user.getRoles() != Roles.USER_ADMIN )
        {
            throw new AccessDeniedException("Accesso Negado!");
        }

    }

    public void currentUserVerifyByID(Integer idUser) throws AccessDeniedException {

        UsersEntity user = this.getCurrentAuthUser();

        //Caso não seja o atual usuário
        if ( !user.getIdUser().equals(idUser) ) {
            throw new AccessDeniedException("Acesso Negado!");
        }

    }

    public void currentUserVerifyByEmail(String emailUser) throws AccessDeniedException {

        UsersEntity user = this.getCurrentAuthUser();

        //Caso não seja o atual usuário
        if (  !user.getUserEmail().equalsIgnoreCase(emailUser)) {
            throw new AccessDeniedException("Acesso Negado!");
        }

    }





}
