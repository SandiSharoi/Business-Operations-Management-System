package com.DAT.capstone_project.repository;

import com.DAT.capstone_project.model.AssignApproverEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignApproverRepository extends JpaRepository<AssignApproverEntity, Long> {
    
    // Correct way to reference the formApplyId through the associated FormApplyEntity
    List<AssignApproverEntity> findByFormApply_FormApplyId(Long formApplyId);

    // Get all forms for the current approver from assign_approver table (all forms = AssignApproverEntites)
    List<AssignApproverEntity> findByApproverId(Long approverId);

    Optional<AssignApproverEntity> findByFormApply_FormApplyIdAndApproverId(Long formApplyId, Long approverId);

    // Custom query to find assign approvers by form status
    List<AssignApproverEntity> findByFormStatus(String formStatus);

    Optional<AssignApproverEntity> findByFormApply_FormApplyIdAndApproverPosition(Long formApplyId, String approverPosition);

}
