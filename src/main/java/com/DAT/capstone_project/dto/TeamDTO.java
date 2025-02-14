package com.DAT.capstone_project.dto;

import lombok.Data;
import lombok.ToString;

@Data
public class TeamDTO {

    private Integer id;
    private String name;
    
    private DepartmentDTO department;  // Use DepartmentDTO instead of DepartmentEntity
    
    @ToString.Exclude
    private UsersDTO pm;  // Use UsersDTO instead of UsersEntity

    @ToString.Exclude
    private UsersDTO dh;  // Use UsersDTO instead of UsersEntity
    
    @ToString.Exclude
    private UsersDTO divh;  // Use UsersDTO instead of UsersEntity

    // private Integer depId;

    private Integer departmentId;

}
