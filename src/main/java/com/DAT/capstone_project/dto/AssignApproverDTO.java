package com.DAT.capstone_project.dto;

import lombok.Data;

@Data
public class AssignApproverDTO {

    private Long id;
    private Long formApplyId; // ID of the FormApply entity
    private Long approverId; // ID of the approver (UsersEntity)
    private String approverPosition; // Position of the approver (PM, DH, DivH)
    private String formStatus; // Status of the form (Pending, Accepted, Rejected)
    private boolean checked = true;  // This ensures all checkboxes are checked by default.
}
