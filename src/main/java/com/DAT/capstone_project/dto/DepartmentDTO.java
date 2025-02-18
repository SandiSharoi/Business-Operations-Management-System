package com.DAT.capstone_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor  // Generates a constructor with all fields as arguments
@NoArgsConstructor   // Generates a no-argument constructor (optional)
public class DepartmentDTO {
    private Integer id;
    private String name;
}
