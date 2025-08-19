package com.utility.company.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Facility {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    @Size(min = 8, max = 64)
    private String name;

    @Column(nullable = false)
    private Integer price;

    @OneToMany(mappedBy = "facility")
    private List<EquipmentFacility> equipments;
}
