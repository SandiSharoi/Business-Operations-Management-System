package com.DAT.capstone_project.service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.DAT.capstone_project.dto.FormApplyDTO;
import com.DAT.capstone_project.dto.UsersDTO;
import com.DAT.capstone_project.model.AssignApproverEntity;
import com.DAT.capstone_project.model.FormApplyEntity;
import com.DAT.capstone_project.repository.AssignApproverRepository;
import com.DAT.capstone_project.repository.FormApplyRepository;



@Service
public class ApproverService {

    @Autowired
    private AssignApproverRepository assignApproverRepository;

    @Autowired
    private FormApplyRepository formApplyRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public List<FormApplyDTO> getVisibleFormsForApprover(Long approverId) {
        // Fetch forms assigned to the current approver
        List<AssignApproverEntity> assignedForms = assignApproverRepository.findByApproverId(approverId);
    
        return assignedForms.stream()
            // Filter out forms that are already approved or rejected
            .filter(assignApprover -> 
                "Pending".equalsIgnoreCase(assignApprover.getFormStatus()) && 
                isVisibleToApprover(assignApprover, approverId)
            )
            .map(assignApprover -> {
                FormApplyDTO formDTO = new FormApplyDTO();
                formDTO.setFormApplyId(assignApprover.getFormApply().getFormApplyId());
                formDTO.setTask(assignApprover.getFormApply().getTask());
                formDTO.setAppliedDate(assignApprover.getFormApply().getAppliedDate());
                UsersDTO employeeDTO = modelMapper.map(assignApprover.getFormApply().getEmployee(), UsersDTO.class);
                formDTO.setEmployee(employeeDTO);
                return formDTO;
            })
            .collect(Collectors.toList());
    }
    
    private boolean isVisibleToApprover(AssignApproverEntity assignApprover, Long approverId) {
        FormApplyEntity formApply = assignApprover.getFormApply();
        List<AssignApproverEntity> approvers = assignApproverRepository.findByFormApply_FormApplyId(formApply.getFormApplyId());

        // Determine the positions assigned to this form
        Set<String> assignedPositions = approvers.stream()
            .map(AssignApproverEntity::getApproverPosition)
            .collect(Collectors.toSet());

        String approverPosition = assignApprover.getApproverPosition();

        // If only higher positions are assigned, skip lower positions
        if (isHigherLevelOnly(approverPosition, assignedPositions)) {
            return true;
        }

        // Check if all lower approvers have approved
        boolean allLowerApproversApproved = approvers.stream()
            .filter(a -> isLowerPosition(a.getApproverPosition(), approverPosition))
            .allMatch(a -> "Approve".equalsIgnoreCase(a.getFormStatus()));

        // If any lower approver rejected the form, it should not be visible
        boolean anyLowerApproverRejected = approvers.stream()
            .filter(a -> isLowerPosition(a.getApproverPosition(), approverPosition))
            .anyMatch(a -> "Reject".equalsIgnoreCase(a.getFormStatus()));

        return allLowerApproversApproved && !anyLowerApproverRejected;
    }

    @Transactional
    public void approveForm(Long formId, Long approverId) {
        AssignApproverEntity approver = assignApproverRepository
            .findByFormApply_FormApplyIdAndApproverId(formId, approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not assigned to this form"));
    
        approver.setFormStatus("Approve");
        assignApproverRepository.save(approver);
    
        // Check if this approver's position is the highest approver
        FormApplyEntity formApply = formApplyRepository.findById(formId)
            .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
    
        if (approver.getApproverPosition().equalsIgnoreCase(formApply.getHighest_approver())) {
            formApply.setFinalFormStatus("Approve");
            formApplyRepository.save(formApply);
            System.out.println("AAAAAAAAAAAAAAAAAAAA");
        }
    }
    

    @Transactional
    public void rejectForm(Long formId, Long approverId) {
        AssignApproverEntity approver = assignApproverRepository
            .findByFormApply_FormApplyIdAndApproverId(formId, approverId)
            .orElseThrow(() -> new IllegalArgumentException("Approver not assigned to this form"));
    
        approver.setFormStatus("Reject");
        assignApproverRepository.save(approver);
    
        // Automatically reject for all higher approvers
        List<AssignApproverEntity> higherApprovers = assignApproverRepository
            .findByFormApply_FormApplyId(formId).stream()
            .filter(a -> isHigherPosition(a.getApproverPosition(), approver.getApproverPosition()))
            .collect(Collectors.toList());
    
        higherApprovers.forEach(higherApprover -> {
            higherApprover.setFormStatus("Reject");
            assignApproverRepository.save(higherApprover);
            System.out.println("All Reject");
        });
    
        // Check if this approver's position is the highest approver
        FormApplyEntity formApply = formApplyRepository.findById(formId)
            .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
            System.out.println("Before if approver check");
            
            System.out.println("approver po:::::::" + approver.getApproverPosition() );
            System.out.println("highest approver po:::::::" + formApply.getHighest_approver() );


        
            formApply.setFinalFormStatus("Reject");
            formApplyRepository.save(formApply);
            System.out.println("RRRRRRRRRRRRRRRRRRRRRRR");
        
    }
    

    private boolean isHigherPosition(String higherPosition, String currentPosition) {
        List<String> hierarchy = Arrays.asList("PM", "DH", "DivH");
        int higherIndex = hierarchy.indexOf(higherPosition);
        int currentIndex = hierarchy.indexOf(currentPosition);
        return higherIndex > currentIndex;
    }

    private boolean isHigherLevelOnly(String currentPosition, Set<String> assignedPositions) {
        List<String> hierarchy = Arrays.asList("PM", "DH", "DivH");
        int currentIndex = hierarchy.indexOf(currentPosition);

        return assignedPositions.stream()
            .allMatch(position -> hierarchy.indexOf(position) > currentIndex);
    }

    private boolean isLowerPosition(String lowerPosition, String currentPosition) {
        List<String> hierarchy = Arrays.asList("PM", "DH", "DivH");
        int lowerIndex = hierarchy.indexOf(lowerPosition);
        int currentIndex = hierarchy.indexOf(currentPosition);
        return lowerIndex < currentIndex;
    }

}