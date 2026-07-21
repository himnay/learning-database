package com.learning.database.inheritance.entity;

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
