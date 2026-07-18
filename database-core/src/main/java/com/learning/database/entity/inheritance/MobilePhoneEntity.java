package com.learning.database.entity.inheritance;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "mobile_phone")
public class MobilePhoneEntity extends DeviceBase {

    private String color;
}
