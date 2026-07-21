package com.learning.database.inheritance.repository;

import com.learning.database.inheritance.entity.CarEntity;
import com.learning.database.inheritance.entity.VehicleEntityInheritanceType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<VehicleEntityInheritanceType, Long> {

    /**
     * Derived query that finds all vehicles matching the given brand.
     * Polymorphic query: returns both Car and Motorcycle rows, since the entity
     * hierarchy uses the SINGLE_TABLE inheritance strategy (no UNION needed, fast).
     *
     * @param brand the vehicle brand to match
     * @return list of vehicles (of any subtype) with the given brand
     */
    List<VehicleEntityInheritanceType> findByBrand(String brand);

    /**
     * Derived query that finds cars matching the given brand and number of doors.
     * Subclass-specific query: Spring Data narrows the result to CarEntity rows automatically.
     *
     * @param brand the vehicle brand to match
     * @param numDoors the number of doors to match
     * @return list of cars with the given brand and number of doors
     */
    List<CarEntity> findByBrandAndNumDoors(String brand, Integer numDoors);
}
