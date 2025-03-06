package com.DAT.capstone_project.controller;

import java.util.List;
import java.util.Map;

import com.DAT.capstone_project.dto.DepartmentDTO;
import com.DAT.capstone_project.model.UsersEntity;
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

    //Show User Registration Page.........................................................................
    @GetMapping("/user_registration")
    public String showRegistrationPage(Model model) {
        return adminService.showRegistrationPage(model);
    }


    //Show Departments which has pm_id = null or dh_id = null or divh_id = null according to Approver Position Registration...........................................
//    Handles AJAX request (fetch in JavaScript) to /get_departments
    @GetMapping("/get_departments")
    @ResponseBody
    public List<DepartmentDTO> getDepartments(@RequestParam String position) {
        //Returns a list of departments as JSON for dynamic population in the frontend.
        return adminService.getDepartmentsForPosition(position);
    }


    @PostMapping("/submit_registration")
    public String registerUser(@ModelAttribute("user") UsersDTO usersDTO, Model model) {
        return adminService.registerUser(usersDTO, model);  // Pass DTO instead of entity
    }


    @GetMapping("/registration_list")
    public String showRegistrationListPage(Model model) {
        return adminService.showRegistrationListPage(model);
    }


    // User details View .........................................................
    @GetMapping("/user/view/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        Map<String, Object> data = adminService.getUserDetailsView(id, false); // View-only mode (false)
        model.addAttribute("user", data.get("user"));
        model.addAttribute("positions", data.get("positions"));
        model.addAttribute("teams", data.get("teams"));
        model.addAttribute("roles", data.get("roles"));

        // Add departmentIds or departments
        if (data.containsKey("departmentIds")) {
            model.addAttribute("departmentIds", data.get("departmentIds"));
        }

        // ✅ Ensure departments list is always added
        model.addAttribute("departments", departmentRepository.findAll());

        return "user_view"; // Loads user_view.html
    }

// User details Edit .........................................................

    @GetMapping("/user/edit/{id}")
    public String editUserDetails(@PathVariable Long id, Model model) {
        Map<String, Object> data = adminService.getUserDetailsEdit(id, true); // Edit mode (true)
        model.addAttribute("user", data.get("user"));
        model.addAttribute("positions", data.get("positions"));
        model.addAttribute("teams", data.get("teams"));
        model.addAttribute("roles", data.get("roles"));

        // ✅ Pass departmentIds for Division Head
        if (data.containsKey("departmentIds")) {
            model.addAttribute("departmentIds", data.get("departmentIds"));
        }

        // ✅ Use the refined list of departments from getUserDetailsEdit
        if (data.containsKey("departments")) {
            model.addAttribute("departments", data.get("departments"));
        }

        // ✅ Pass assigned department IDs for highlighting
        if (data.containsKey("assignedDepartmentIds")) {
            model.addAttribute("assignedDepartmentIds", data.get("assignedDepartmentIds"));
        }

        return "user_edit"; // Loads user_edit.html
    }



    @PostMapping("/user/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("user") UsersDTO userDTO, Model model) {
        // Check if the email already exists (excluding the current user)
        if (adminService.isEmailTaken(  userDTO.getEmail(), id)) {
            model.addAttribute("emailError", "This email is already in use. Please use a different email.");

            // Get user details + dropdown lists
            Map<String, Object> data = adminService.getUserDetailsView(id, true);
            model.addAllAttributes(data);

            model.addAttribute("user", userDTO);  // Add user DTO back to the model
            return "users_edit";  // Return to the same page
        }

        // Proceed with updating the user
        adminService.updateUser(userDTO);
        return "registration_list_success";  // Redirect to the success page
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
        return "user_edit";

    }

    // Search employee in registration list by name or id...................................................
    @GetMapping("/search")
    public String searchUsers(@RequestParam(required = false) String idQuery,
                              @RequestParam(required = false) String nameQuery,
                              Model model) {
        List<UsersDTO> users = adminService.searchUsersByIdOrName(idQuery, nameQuery);
        model.addAttribute("users", users);
        return "registration_list"; // Ensure this matches your Thymeleaf template name
    }


    // User Info & Password change................................................................................
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        UsersDTO user = (UsersDTO) session.getAttribute("loggedInUser");

        // Ensure the user object is not null and has valid role and position
        if (user != null) {
            String roleName = String.valueOf(user.getRole());
            String position = String.valueOf(user.getPosition());

            // Call the method to determine the menu based on the role and position
            return adminService.redirectBasedOnRole(roleName, position, model);
        }

        // If no user is found, redirect to login page
        return "login";
    }

    @PostMapping("user/{id}/change-password")
    public String changePassword(@PathVariable("id") Long userId,
                                 @RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 HttpSession session,
                                 Model model) {

        // Validate passwords
        String errorMessage = adminService.validatePasswordChange(userId, oldPassword, newPassword, confirmPassword);

        if (errorMessage != null) {
            model.addAttribute("error", errorMessage);
            UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");

            String roleName = loggedInUser.getRole().getName();
            String positionName = loggedInUser.getPosition().getName();

            // Redirect based on role and position
            return adminService.redirectBasedOnRole(roleName, positionName , model);        }

        // Change password
        boolean isPasswordChanged = adminService.changePassword(userId, newPassword);

        if (isPasswordChanged) {

            model.addAttribute("message", "Password changed successfully!");

            UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");

            String roleName = loggedInUser.getRole().getName();
            String positionName = loggedInUser.getPosition().getName();

            // Redirect based on role and position
            return adminService.redirectBasedOnRole(roleName, positionName , model);

        } else {
            model.addAttribute("error", errorMessage);
            UsersEntity loggedInUser = (UsersEntity) session.getAttribute("loggedInUser");

            String roleName = loggedInUser.getRole().getName();
            String positionName = loggedInUser.getPosition().getName();

            // Redirect based on role and position
            return adminService.redirectBasedOnRole(roleName, positionName, model);
        }
    }
}