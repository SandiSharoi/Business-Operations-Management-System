package com.DAT.capstone_project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "assign_approver")
public class AssignApproverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "form_apply_id", nullable = false) // Foreign key to FormApply
    private FormApplyEntity formApply;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "approver_id", nullable = false) // Foreign key to UsersEntity (approver)
    private UsersEntity approver;

    private String approverPosition; // Position of the approver (PM, DH, DivH)
    private String formStatus; // Status of the form (Pending, Accepted, Rejected)
    
}