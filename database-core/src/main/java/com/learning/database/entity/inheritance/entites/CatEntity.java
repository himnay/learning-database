package com.learning.database.entity.inheritance.entites;

import com.learning.database.entity.inheritance.AnimalEntityInheritanceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cat")
public class CatEntity extends AnimalEntityInheritanceType {

    private String color;
}
