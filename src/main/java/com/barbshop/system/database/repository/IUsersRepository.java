package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.UsersEntity;
import com.barbshop.system.responses.UsersResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IUsersRepository extends JpaRepository<UsersEntity, Integer> {

    @Override
    Optional<UsersEntity> findById(Integer integer);

    Optional<UsersEntity> findByUserEmail(String email);

    @Query(value = "SELECT u.user_nome, " +
            "u.user_email, " +
            "u.telefone_cliente " +
            "FROM usuarios u", nativeQuery = true)
    Page<UsersResponse> findAllUsersResponses(Pageable pageable);
}
