package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "position")
@NoArgsConstructor
@AllArgsConstructor // Allows object creation with parameters
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name; // Assuming the position name is stored in a column

    // Constructor without id (useful for creating new positions)
    public PositionEntity(String name) {
        this.name = name;
    }
}