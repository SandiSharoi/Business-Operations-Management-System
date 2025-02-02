package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "position")
public class PositionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name; // Assuming the role name is stored in a column
}