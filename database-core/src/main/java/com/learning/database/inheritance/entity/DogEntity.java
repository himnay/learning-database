package com.learning.database.inheritance.entity;

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
