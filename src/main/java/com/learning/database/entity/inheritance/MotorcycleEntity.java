package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("Motorcycle")
@Getter
@Setter
public class MotorcycleEntity extends VehicleEntity {

    @Column(name = "engine_capacity_cc")
    private Integer engineCapacityCc;
}
