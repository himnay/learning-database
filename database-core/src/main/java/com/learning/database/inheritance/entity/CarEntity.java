package com.learning.database.inheritance.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Car")
public class CarEntity extends VehicleEntityInheritanceType {

    @Column(name = "num_doors")
    private Integer numDoors;
}
