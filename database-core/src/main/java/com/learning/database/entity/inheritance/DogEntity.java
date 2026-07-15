package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dog")
@Getter
@Setter
public class DogEntity extends AnimalEntity {

    private String breed;
}
