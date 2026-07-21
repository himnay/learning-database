package com.learning.database.inheritance.repository;

import com.learning.database.inheritance.entity.AnimalEntityInheritanceType;
import com.learning.database.inheritance.entity.DogEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<AnimalEntityInheritanceType, Long> {

    /**
     * Finds all animals matching the given name.
     * Derived query equivalent to {@code WHERE name = :name}.
     *
     * @param name the animal name to search for
     * @return list of matching {@link AnimalEntityInheritanceType} entities
     */
    List<AnimalEntityInheritanceType> findByName(String name);

    /**
     * Finds all dogs matching the given breed.
     * Derived query equivalent to {@code WHERE breed = :breed}.
     *
     * @param breed the dog breed to search for
     * @return list of matching {@link DogEntity} entities
     */
    List<DogEntity> findByBreed(String breed);
}
