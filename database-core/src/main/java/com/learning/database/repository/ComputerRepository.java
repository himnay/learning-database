package com.learning.database.repository;

import com.learning.database.entity.inheritance.entites.ComputerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComputerRepository extends JpaRepository<ComputerEntity, Long> {

    // MappedSuperclass: no polymorphic query possible — each entity has its own repo
    List<ComputerEntity> findByOs(String os);
}
