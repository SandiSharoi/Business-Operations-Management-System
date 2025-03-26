package com.DAT.capstone_project.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;



@Data // Lombok annotation for getters, setters, and other methods
public class FormApplyDTO {

    private Long formApplyId;
    private UsersDTO employee;
    private LocalDate appliedDate;

    private String projectId;

    private String projectName;

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

    private String formStatus;

    public String getDuration() {
        if (this.actualStartHour != null && this.actualEndHour != null) {
            Duration duration = Duration.between(this.actualStartHour, this.actualEndHour);

            // If duration is zero, log this case for review
            if (duration.isZero()) {
                return "Same Start and End Hour";
            }

            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;

            // Handle negative durations (e.g., if the end time is earlier than the start time)
            if (duration.isNegative()) {
                return "Invalid Time Range";
            }

            return String.format("%d hours: %02d minutes", hours, minutes);
        }
        return "N/A"; // If either value is null
    }
}