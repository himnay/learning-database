package com.learning.database.entity.inheritance.entites;

import com.learning.database.entity.inheritance.VehicleEntityInheritanceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DiscriminatorValue("Motorcycle")
public class MotorcycleEntity extends VehicleEntityInheritanceType {

    @Column(name = "engine_capacity_cc")
    private Integer engineCapacityCc;
}
