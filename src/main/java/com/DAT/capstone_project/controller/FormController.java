package com.DAT.capstone_project.controller;

import com.DAT.capstone_project.dto.AssignApproverDTO;
import com.DAT.capstone_project.dto.FormApplyDTO;
import com.DAT.capstone_project.model.UsersEntity;
import com.DAT.capstone_project.service.FormService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequiredArgsConstructor
public class FormController {

    @Autowired
    private FormService formApplyService;


    // Display the form to apply for overtime
    @GetMapping("/form_apply")
    public String getApplyOvertimeForm(Model model, HttpSession session) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
    
        if (loggedInUser == null) {
            return "redirect:/"; // Redirect to login if not logged in
        }
    
        // Fetch approvers for the logged-in user
        List<AssignApproverDTO> approvers = formApplyService.getApproversForUser(loggedInUser);

        // Add the form, approvers, and user's position to the model
        model.addAttribute("formApplyDTO", new FormApplyDTO());
        model.addAttribute("approvers", approvers);
  
        return "form_apply"; // Load the Thymeleaf template


    }    

    @PostMapping("/submit_form_apply")
    public String submitFormApply(@ModelAttribute("formApplyDTO") FormApplyDTO formApplyDTO,
                                  HttpSession session,
                                  Model model) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
    
        if (loggedInUser == null) {
            return "redirect:/"; // Redirect to login if not logged in
        }
    
        String userPosition = loggedInUser.getPosition().getName();
        List<String> assignedApprovers = formApplyDTO.getAssignedApprovers();
    
        List<String> hierarchy = Arrays.asList("PM", "DH", "DivH");
        int userIndex = hierarchy.indexOf(userPosition);
        List<String> validApprovers = hierarchy.subList(userIndex + 1, hierarchy.size());
    
        for (int i = 0; i < validApprovers.size(); i++) {
            String currentApprover = validApprovers.get(i);
            if (assignedApprovers.contains(currentApprover)) {
                for (int j = 0; j < i; j++) {
                    String lowerApprover = validApprovers.get(j);
                    if (!assignedApprovers.contains(lowerApprover)) {
                        model.addAttribute("error", currentApprover + " cannot be selected without " + lowerApprover + ".");
                        retainFormState(loggedInUser, formApplyDTO, model);
                        return "form_apply";
                    }
                }
            }
        }
    
        try {
            // Determine the highest approver
            String highestApprover = determineHighestApprover(assignedApprovers, hierarchy);
    
            // Save the form with the highest approver
            formApplyService.saveFormApply(formApplyDTO, assignedApprovers, highestApprover, loggedInUser);
            return "form_apply_success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while submitting the form. Please try again.");
            retainFormState(loggedInUser, formApplyDTO, model);
            return "form_apply";
        }
    }
    
    private String determineHighestApprover(List<String> assignedApprovers, List<String> hierarchy) {
        for (int i = hierarchy.size() - 1; i >= 0; i--) {
            if (assignedApprovers.contains(hierarchy.get(i))) {
                return hierarchy.get(i);
            }
        }
        return null;
    }
    
        
    private void retainFormState(UsersEntity loggedInUser, FormApplyDTO formApplyDTO, Model model) {
        model.addAttribute("formApplyDTO", formApplyDTO);
    
        // Repopulate approvers list
        List<AssignApproverDTO> approvers = formApplyService.getApproversForUser(loggedInUser);
        model.addAttribute("approvers", approvers);
    }
    

    // Check Form Status......................................................

    @GetMapping("/check_form_status")
    public String checkFormStatus(Model model, HttpSession session) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login"; // Redirect to login if user is not logged in
        }

        List<FormApplyDTO> forms = formApplyService.getFormsByEmployee(loggedInUser);
        model.addAttribute("forms", forms);
        return "check_form_status";
    }

    // Form Apply Detail View.............................................................

    @GetMapping("/form/detail/{formId}")
    public String viewFormDetails(@PathVariable Long formId, @RequestParam String source, 
                              Model model) {
    FormApplyDTO formDetails = formApplyService.getFormDetails(formId);
    model.addAttribute("formApplyDTO", formDetails);
    model.addAttribute("source", source);
    return "form_apply_detail";

    }



    // Form Apply Detail Edit.............................................................

    // Load form apply edit page with pre-filled data
    @GetMapping("/form_apply_edit/{id}")
    public String editFormApply(@PathVariable("id") Long formApplyId, Model model) {
        FormApplyDTO formApplyDTO = formApplyService.getFormApplyById(formApplyId);
        formApplyDTO.setPlannedDate(LocalDate.now());
        model.addAttribute("formApplyDTO", formApplyDTO);
        return "form_apply_edit";
    }

    // Save the edited form
    @PostMapping("/update_form_apply")
    public String updateFormApply(@ModelAttribute("formApplyDTO") FormApplyDTO formApplyDTO, RedirectAttributes redirectAttributes) {
        try {
            formApplyService.updateFormApply(formApplyDTO);
            redirectAttributes.addFlashAttribute("success", "Form updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update form: " + e.getMessage());
        }
        return "redirect:/check_form_status"; // Redirect to form list or any appropriate page
    }


}