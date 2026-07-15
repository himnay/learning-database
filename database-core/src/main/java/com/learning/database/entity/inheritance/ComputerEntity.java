package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "computer")
@Getter
@Setter
public class ComputerEntity extends DeviceBase {

    private String os;
}
