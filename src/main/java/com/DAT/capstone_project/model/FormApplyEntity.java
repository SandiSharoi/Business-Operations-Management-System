package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data // Lombok annotation to generate getters, setters, toString, equals, and hashCode
@Table(name = "form_apply")
public class FormApplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long formApplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id") // Foreign key column
    private UsersEntity employee; // Relationship with UsersEntity

    @Column(columnDefinition = "DATE")
    private LocalDate appliedDate;

    private String task;

    @Column(columnDefinition = "DATE") // Explicitly specify that this column is of type DATE
    private LocalDate plannedDate;

    @Column(columnDefinition = "TIME") // Explicitly specify that this column is of type TIME
    private LocalTime plannedStartHour;

    @Column(columnDefinition = "TIME") // Explicitly specify that this column is of type TIME
    private LocalTime plannedEndHour;

    @Column(columnDefinition = "DATE") // Explicitly specify that this column is of type DATE
    private LocalDate actualDate;

    @Column(columnDefinition = "TIME") // Explicitly specify that this column is of type TIME
    private LocalTime actualStartHour;

    @Column(columnDefinition = "TIME") // Explicitly specify that this column is of type TIME
    private LocalTime actualEndHour;

    private String overtimeDate; // Changed to a single String stored in the same table

    private String workType; // New field for work type    
    
    private String description;

    private Integer no_of_approvers;

    private String highest_approver;

    private String finalFormStatus; // Pending, Approved, Rejected

    @PrePersist
    public void prePersist() {
        if (this.appliedDate == null) {
            this.appliedDate = LocalDate.now(); // Set to current date
        }
    }
}