package com.DAT.capstone_project.controller;

import com.DAT.capstone_project.dto.AssignApproverDTO;
import com.DAT.capstone_project.dto.FormApplyDTO;
import com.DAT.capstone_project.model.UsersEntity;
import com.DAT.capstone_project.service.FormService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


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
                                  @RequestParam("documentFile") MultipartFile file,
                                  HttpSession session,
                                  Model model) {
        UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/";
        }

        String userPosition = loggedInUser.getPosition().getName();
        List<String> assignedApprovers = formApplyDTO.getAssignedApprovers();
        List<String> hierarchy = Arrays.asList("Project Manager", "Department Head", "Division Head");
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

        // file attachment.......................................................
        try {
            String highestApprover = determineHighestApprover(assignedApprovers, hierarchy);

            //  Save form first to generate form_id
            Long formId = formApplyService.saveFormApply(formApplyDTO, assignedApprovers, highestApprover, loggedInUser);



            //  Handle File Upload with Correct Naming
            if (file != null && !file.isEmpty()) {
                try {
                    String UPLOAD_DIR = "src/main/resources/uploads";
                    LocalDate now = LocalDate.now();
                    String year = String.valueOf(now.getYear());
                    String month = String.format("%02d", now.getMonthValue());

                    Path uploadPath = Paths.get(UPLOAD_DIR, year, month);
                    Files.createDirectories(uploadPath); //  Ensures directories exist

                    String originalFileName = file.getOriginalFilename();
                    String fileExtension = (originalFileName != null && originalFileName.contains("."))
                            ? originalFileName.substring(originalFileName.lastIndexOf("."))
                            : "";

                    //  File Naming Format: <user_id>-<form_id>-attachment.fileextension
                    String newFileName = "U" + loggedInUser.getId() + "-" + "F" + formId + "-attachment" + fileExtension;
                    Path targetLocation = uploadPath.resolve(newFileName);
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    //  Update Form Record with the Correct File Name
                    formApplyService.updateFormFileName(formId, newFileName);

                } catch (IOException e) {
                    model.addAttribute("error", "File upload failed: " + e.getMessage());
                    retainFormState(loggedInUser, formApplyDTO, model);
                    return "form_apply";
                }
            }

            return "form_apply_success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while submitting the form. Please try again.");
            retainFormState(loggedInUser, formApplyDTO, model);
            return "form_apply";
        }

        // .........................................................
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

    // Form Apply Detail
    // View.............................................................

    @GetMapping("/form/detail/{formId}")
    public String viewFormDetails(@PathVariable Long formId, @RequestParam String source,
                                  Model model) {
        FormApplyDTO formDetails = formApplyService.getFormDetails(formId);
        model.addAttribute("formApplyDTO", formDetails);
        model.addAttribute("source", source);
        return "form_apply_detail";
    }

    // Form Apply Detail
    // Edit.............................................................

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
    public String updateFormApply(@ModelAttribute("formApplyDTO") FormApplyDTO formApplyDTO,
                                  @RequestParam(value = "documentFile", required = false) MultipartFile file,
                                  RedirectAttributes redirectAttributes,
                                  HttpSession session) {
        try {
            UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");
            if (loggedInUser == null) {
                return "redirect:/";
            }

            // Save the form first to generate formId
            formApplyService.updateFormApply(formApplyDTO);

            // Handle File Upload (If New File Is Provided)
            if (file != null && !file.isEmpty()) {
                try {
                    String UPLOAD_DIR = "src/main/resources/uploads";
                    LocalDate now = LocalDate.now();
                    String year = String.valueOf(now.getYear());
                    String month = String.format("%02d", now.getMonthValue());

                    Path uploadPath = Paths.get(UPLOAD_DIR, year, month);
                    Files.createDirectories(uploadPath); // Ensure directories exist

                    String originalFileName = file.getOriginalFilename();
                    String fileExtension = (originalFileName != null && originalFileName.contains("."))
                            ? originalFileName.substring(originalFileName.lastIndexOf("."))
                            : "";

                    // Generate file name in the format <user_id>-<form_id>-attachment.fileextension
                    Long formId = formApplyDTO.getFormApplyId(); // Assuming the formApplyId is set when editing
                    String newFileName = loggedInUser.getId() + "-" + formId + "-attachment" + fileExtension;
                    Path targetLocation = uploadPath.resolve(newFileName);

                    // Delete Old File Before Uploading New One (if file already exists)
                    if (formApplyDTO.getFileName() != null) {
                        Path oldFile = uploadPath.resolve(formApplyDTO.getFileName());
                        if (Files.exists(oldFile)) {
                            Files.delete(oldFile);
                        }
                    }

                    // Copy the new file to the target location, overwriting any existing file
                    Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                    // Update the form with the new file name
                    formApplyDTO.setFileName(newFileName);

                } catch (IOException e) {
                    redirectAttributes.addFlashAttribute("error", "File upload failed: " + e.getMessage());
                    return "redirect:/form_apply_edit/" + formApplyDTO.getFormApplyId();
                }
            }

            // Update the form record with the new file name
            formApplyService.updateFormFileName(formApplyDTO.getFormApplyId(), formApplyDTO.getFileName());

            // Add a success message to the redirect attributes
            redirectAttributes.addFlashAttribute("success", "Form updated successfully!");
        } catch (Exception e) {
            // Handle unexpected errors during the form update process
            redirectAttributes.addFlashAttribute("error", "Failed to update form: " + e.getMessage());
        }
        return "redirect:/check_form_status";
    }





    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        // Extract the year and month dynamically
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String month = String.format("%02d", now.getMonthValue());

        // Define the file path
        Path filePath = Paths.get("src/main/resources/uploads", year, month, fileName);

        // Check if file exists
        File file = filePath.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // Load file as resource
        Resource resource = new FileSystemResource(file);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

}
