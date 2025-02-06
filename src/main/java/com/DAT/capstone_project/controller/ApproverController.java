package com.DAT.capstone_project.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.DAT.capstone_project.dto.FormApplyDTO;
import com.DAT.capstone_project.model.UsersEntity;
import com.DAT.capstone_project.service.ApproverService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ApproverController {

    @Autowired
    private ApproverService approverService;

    @GetMapping("/form_decision")
    public String showFormList(Model model, HttpSession session) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
        Long approverId = loggedInUser.getId();

        // Get forms visible to the logged-in approver
        List<FormApplyDTO> forms = approverService.getVisibleFormsForApprover(approverId);
        model.addAttribute("forms", forms);

        return "approver_form_list"; // Thymeleaf template
    }

    @PostMapping("/approveForm/{formId}")
    public ResponseEntity<List<FormApplyDTO>> approveForm(@PathVariable Long formId, HttpSession session) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
        Long approverId = loggedInUser.getId();

        approverService.approveForm(formId, approverId);

        // Return updated form list
        List<FormApplyDTO> updatedForms = approverService.getVisibleFormsForApprover(approverId);
        return ResponseEntity.ok(updatedForms);
    }

    @PostMapping("/rejectForm/{formId}")
    public ResponseEntity<List<FormApplyDTO>> rejectForm(@PathVariable Long formId, HttpSession session) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
        Long approverId = loggedInUser.getId();

        approverService.rejectForm(formId, approverId);

        // Return updated form list
        List<FormApplyDTO> updatedForms = approverService.getVisibleFormsForApprover(approverId);
        return ResponseEntity.ok(updatedForms);
    }
}