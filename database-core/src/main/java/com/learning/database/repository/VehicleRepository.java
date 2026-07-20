package com.learning.database.repository;

import com.learning.database.entity.inheritance.entites.CarEntity;
import com.learning.database.entity.inheritance.VehicleEntityInheritanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntityInheritanceType, Long> {

    // Polymorphic query: returns both Car and Motorcycle rows (SINGLE_TABLE — no UNION, fast)
    List<VehicleEntityInheritanceType> findByBrand(String brand);

    // Subclass-specific query: Spring Data narrows to Cars automatically
    List<CarEntity> findByBrandAndNumDoors(String brand, Integer numDoors);
}
