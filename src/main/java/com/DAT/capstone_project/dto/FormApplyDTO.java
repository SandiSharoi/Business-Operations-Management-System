package com.DAT.capstone_project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;



@Data // Lombok annotation for getters, setters, and other methods
public class FormApplyDTO {

    private Long formApplyId;
    private UsersDTO employee;
    private LocalDate appliedDate;
    private String task;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate plannedDate;
    
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime plannedStartHour;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime plannedEndHour;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate actualDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime actualStartHour;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime actualEndHour;
    
    private String overtimeDate; 
    private String workType; 
    private String description;
    private Integer no_of_approvers;
    private String highest_approver;
    private String finalFormStatus;
 
    // Field to store the name of the uploaded file
    private String fileName;  // This field will store the name of the uploaded file
    
    // A list of approver positions (PM, DH, DivH) selected in the form
    private List<String> assignedApprovers;

    private Boolean editPermission;

}