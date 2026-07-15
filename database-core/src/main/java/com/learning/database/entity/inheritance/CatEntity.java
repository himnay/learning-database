package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cat")
@Getter
@Setter
public class CatEntity extends AnimalEntity {

    private String color;
}
