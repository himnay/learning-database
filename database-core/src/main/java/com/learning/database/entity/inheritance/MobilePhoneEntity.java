package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "mobile_phone")
@Getter
@Setter
public class MobilePhoneEntity extends DeviceBase {

    private String color;
}
