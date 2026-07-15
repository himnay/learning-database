package com.learning.database.repository;

import com.learning.database.entity.inheritance.AnimalEntity;
import com.learning.database.entity.inheritance.DogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<AnimalEntity, Long> {

    // TABLE_PER_CLASS: polymorphic query works but Hibernate uses UNION ALL internally —
    // avoid on large datasets (scans all subclass tables every time)
    List<AnimalEntity> findByName(String name);

    List<DogEntity> findByBreed(String breed);
}
