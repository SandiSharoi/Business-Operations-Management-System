package com.DAT.capstone_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Getter
@Setter
//@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    private Long id;
    private String name;
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "\\d+", message = "Phone number must contain only numbers")
    private String phone;

    private String password; // Keep this for login or registration, but exclude from entity

    private PositionDTO position;

    @ToString.Exclude
    private TeamDTO team;

    private DepartmentDTO department;    

    private List<Integer> departmentIds; // ✅ Add this to store multiple department IDs for DivH

    private RoleDTO role;

        // Custom constructor for id and name (for the query)
        public UsersDTO(Long id, String name) {
            this.id = id;
            this.name = name;
        }

    // ✅ Add a constructor to initialize the departmentIds list
    public UsersDTO() {
        this.departmentIds = new ArrayList<>();
    }

}