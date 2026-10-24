package com.barbshop.system.database.repository;

import com.barbshop.system.database.entity.CortesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICortesRepository extends JpaRepository <CortesEntity, Integer>
{
}
