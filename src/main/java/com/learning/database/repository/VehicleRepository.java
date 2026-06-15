package com.learning.database.repository;

import com.learning.database.entity.inheritance.CarEntity;
import com.learning.database.entity.inheritance.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {

    // Polymorphic query: returns both Car and Motorcycle rows (SINGLE_TABLE — no UNION, fast)
    List<VehicleEntity> findByBrand(String brand);

    // Subclass-specific query: Spring Data narrows to Cars automatically
    List<CarEntity> findByBrandAndNumDoors(String brand, Integer numDoors);
}
