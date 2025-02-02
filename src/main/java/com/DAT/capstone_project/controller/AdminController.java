package com.DAT.capstone_project.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.DAT.capstone_project.dto.UsersDTO;  // Updated to use DTO
import com.DAT.capstone_project.repository.DepartmentRepository;
import com.DAT.capstone_project.repository.PositionRepository;
import com.DAT.capstone_project.repository.RoleRepository;
import com.DAT.capstone_project.repository.TeamRepository;
import com.DAT.capstone_project.service.AdminService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;


    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;


    @GetMapping("/")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            Model model,
                            HttpSession session) {
        return adminService.authenticateAndRedirect(email, password, model, session);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }


    @GetMapping("/user_registration")
    public String showRegistrationPage(Model model) {
        return adminService.showRegistrationPage(model);
    }

    @PostMapping("/submit_registration")
    public String registerUser(@ModelAttribute("user") UsersDTO usersDTO, Model model) {
        return adminService.registerUser(usersDTO, model);  // Pass DTO instead of entity
    }

    
    @GetMapping("/registration_list")
    public String showRegistrationListPage(Model model) {
        return adminService.showRegistrationListPage(model);
    }    


    // User details View or Edit or Delete........................................................
    @GetMapping("/user/{id}")
    public String viewOrEditUserDetails(@PathVariable Long id, @RequestParam(value = "edit", required = false) boolean edit, Model model) {
        // Fetch data for user details and dropdowns from AdminService
        Map<String, Object> data = adminService.getUserDetailsOrEdit(id, edit);

        // Add the data to the model
        model.addAttribute("user", data.get("user"));
        model.addAttribute("positions", data.get("positions"));
        model.addAttribute("teams", data.get("teams"));
        model.addAttribute("departments", data.get("departments"));
        model.addAttribute("roles", data.get("roles"));
        model.addAttribute("isEditable", data.get("isEditable"));

        return "users_detail"; // The Thymeleaf template to render the user details
    }

    @PostMapping("/user/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") UsersDTO userDTO) {
        adminService.updateUser(userDTO);
        return "registration_list_success";
    }

    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        // Call service to delete the user by ID
        adminService.deleteUser(id);
        return "redirect:/registration_list";  // Redirect back to the registration list page
    }




    // User Details Reset...........................................................................
    @GetMapping("/user/reset/{id}")
    public String resetUserDetails(@PathVariable Long id, Model model) {
        // Fetch the original user details
        UsersDTO originalUser = adminService.getOriginalUserDetails(id);
    
        // Add the user details to the model
        model.addAttribute("user", originalUser);
    
        // Add isEditable flag to the model
        model.addAttribute("isEditable", false);
    
                // Positions, teams, departments, and roles are automatically handled in the service
                model.addAttribute("positions", positionRepository.findAll());
                model.addAttribute("teams", teamRepository.findAll());
                model.addAttribute("departments", departmentRepository.findAll());
                model.addAttribute("roles", roleRepository.findAll());
        
        // Redirect to the user detail page
        return "users_detail";

    }

    // Searh employee in registration list by name or id...................................................
    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String idQuery,
                              @RequestParam(required = false) String nameQuery,
                              Model model) {
        List<UsersDTO> users = adminService.searchUsersByIdOrName(idQuery, nameQuery);
        model.addAttribute("users", users);
        return "registration_list"; // Ensure this matches your Thymeleaf template name
    }

}