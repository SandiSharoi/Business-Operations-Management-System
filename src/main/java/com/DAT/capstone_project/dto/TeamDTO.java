package com.DAT.capstone_project.dto;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.ToString;

@Data
public class TeamDTO {

    private Integer id;
    private String name;
    
    private DepartmentDTO department;  // Use DepartmentDTO instead of DepartmentEntity

    @JsonManagedReference // Prevent recursive serialization of the pm field in TeamDTO
    @ToString.Exclude
    private UsersDTO pm;  // Use UsersDTO instead of UsersEntity

    @JsonManagedReference // Prevent recursive serialization of the dh field in TeamDTO
    @ToString.Exclude
    private UsersDTO dh;  // Use UsersDTO instead of UsersEntity

    @JsonManagedReference // Prevent recursive serialization of the divh field in TeamDTO
    @ToString.Exclude
    private UsersDTO divh;  // Use UsersDTO instead of UsersEntity
}
