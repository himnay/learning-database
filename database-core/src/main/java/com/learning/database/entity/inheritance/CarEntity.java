package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Car")
public class CarEntity extends VehicleEntity {

    @Column(name = "num_doors")
    private Integer numDoors;
}
