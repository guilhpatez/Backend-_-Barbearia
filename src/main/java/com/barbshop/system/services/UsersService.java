package com.barbshop.system.services;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.database.repository.IUsersRepository;
import com.barbshop.system.exception.BadRequest;
import com.barbshop.system.utils.RequestsFilter;
import com.barbshop.system.requests.PasswordChangeRequest;
import com.barbshop.system.requests.UsersRequest;
import com.barbshop.system.responses.UsersResponse;
import com.barbshop.system.enums.Roles;
import com.barbshop.system.exception.NotFoundException;
import com.barbshop.system.security.UserVerifySecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class UsersService
{
    private final IUsersRepository repository;
    private final PasswordEncoder codificador;
    private final UserVerifySecurity verifySecurity;
    private final RequestsFilter filtros;

    //Funções que serão usadas no GET
    @PreAuthorize("hasRole('USER_ADMIN')")
    public Page<UsersResponse> findAll(Pageable pageable) throws NotFoundException {
        //Verificação de usuario
        verifySecurity.adminVerify();

        Page<UsersResponse> pagina = repository.findAllUsersResponses(pageable);

        if (pagina.isEmpty())
            throw new NotFoundException("Nenhum usário encontrado.");

        return pagina;
    }

    @PreAuthorize("hasRole('USER_COMMOM')")
    public UsersResponse findById(Integer id) throws NotFoundException {
        verifySecurity.currentUserVerifyByID(id);

        UsersEntity usuario = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        return new UsersResponse(usuario.getUserNome(), usuario.getUserEmail(), usuario.getTelefone_cliente());
    }

    @PreAuthorize("hasRole('USER_COMMOM')")
    public UsersResponse findByEmail(String email) throws NotFoundException {
        //Verificação de usuário
        verifySecurity.currentUserVerifyByEmail(email);

        UsersEntity usuario = repository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        return new UsersResponse(usuario.getUserNome(), usuario.getUserEmail(), usuario.getTelefone_cliente());
    }


    //Função que será usada no POST
    public void create(UsersRequest usersRequest) throws BadRequest {

        if(!filtros.isPasswordValid(usersRequest.senha()))
            throw new BadRequest("Senha inválida");

        UsersEntity user = UsersEntity.builder()
                .userEmail(usersRequest.email())
                .roles(Roles.USER_COMMOM)
                .userSenha(codificador.encode(usersRequest.senha()))
                .userNome(usersRequest.nome())
                .telefone_cliente(usersRequest.numTel())
                .build();

        repository.save(user);
    }

    //Funções usada no DELETE
    @PreAuthorize("hasRole('USER_COMMOM')")
    public void delete(String email, String password) throws NotFoundException, BadRequest
    {
        //Verificção de usuário
        verifySecurity.currentUserVerifyByEmail(email);

        UsersEntity user = repository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));

        if(!codificador.matches(password, user.getPassword())) {
            throw new BadRequest("Senha inválida");
        }

        repository.delete(user);

    }

    //Funções do PATCH
    @PreAuthorize("hasRole('USER_COMMOM')")
    public void changePassword(PasswordChangeRequest passChangeRequest, String email )
            throws NotFoundException, BadRequest
    {
        verifySecurity.currentUserVerifyByEmail(email);

        UsersEntity usersEntity = repository.findByUserEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));


        if(passChangeRequest.newPassword().isEmpty() || passChangeRequest.newPassword().isBlank())
        {
            throw new BadRequest("Senha vazia");
        }
        usersEntity.setUserSenha(codificador.encode( passChangeRequest.newPassword() ));
        repository.save(usersEntity);

    }

}
