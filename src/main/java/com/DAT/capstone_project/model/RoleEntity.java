package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "role")
@NoArgsConstructor  // Required for JPA
@AllArgsConstructor // Allows object creation with parameters
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name; // Assuming the role name is stored in a column

    // Constructor without id (useful when creating new instances)
    public RoleEntity(String name) {
        this.name = name;
    }
}