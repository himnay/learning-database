package com.learning.database.entity.inheritance.entites;

import com.learning.database.entity.inheritance.AnimalEntityInheritanceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "dog")
public class DogEntity extends AnimalEntityInheritanceType {

    private String breed;
}
