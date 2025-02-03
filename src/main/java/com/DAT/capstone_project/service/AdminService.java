package com.DAT.capstone_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.modelmapper.ModelMapper;

import com.DAT.capstone_project.dto.UsersDTO;  
import com.DAT.capstone_project.model.UsersEntity;
import com.DAT.capstone_project.model.PositionEntity;
import com.DAT.capstone_project.model.TeamEntity;
import com.DAT.capstone_project.model.DepartmentEntity;
import com.DAT.capstone_project.model.RoleEntity;
import com.DAT.capstone_project.repository.UsersRepository;
import com.DAT.capstone_project.repository.PositionRepository;
import com.DAT.capstone_project.repository.TeamRepository;
import com.DAT.capstone_project.repository.DepartmentRepository;
import com.DAT.capstone_project.repository.RoleRepository;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    // --- Authentication and Registration ---
    public Optional<UsersEntity> authenticate(String email, String password) {
        Optional<UsersEntity> user = usersRepository.findByEmail(email);
        return user.filter(u -> u.getPassword().equals(password));
    }


    public String authenticateAndRedirect(String email, String password, Model model, HttpSession session) {
        Optional<UsersEntity> user = authenticate(email, password);
    
        if (user.isPresent()) {
            UsersEntity loggedInUser = user.get();
            session.setAttribute("loggedInUser", loggedInUser); // Store user in session
    
            model.addAttribute("user", modelMapper.map(loggedInUser, UsersDTO.class));
            String roleName = loggedInUser.getRole().getName();
            String positionName = loggedInUser.getPosition().getName();

            return redirectBasedOnRole(roleName, positionName , model);

        } else {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }
    

    public String redirectBasedOnRole(String roleName, String position, Model model) {
        switch (roleName.toUpperCase()) {
            case "ADMIN":
                model.addAttribute("menu", List.of("Department List","Team List", "User Registration", "Registration List", "Form Apply", "Check Form Status"));
                return "dashboard";
            case "APPROVER":
                // Add the check for "DivH" position
                if ("DIVH".equalsIgnoreCase(position)) {
                    model.addAttribute("menu", List.of("Form Decision"));
                } else {
                    model.addAttribute("menu", List.of("Form Apply", "Check Form Status", "Form Decision"));
                }
                return "dashboard";
            case "EMPLOYEE":
                model.addAttribute("menu", List.of("Form Apply", "Check Form Status"));
                return "dashboard";
            default:
                model.addAttribute("error", "Unknown role: " + roleName);
                return "login";
        }
    }
    
    

    public String showRegistrationPage(Model model) {
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("user", new UsersDTO());
        return "registration";
    }

    public String registerUser(UsersDTO usersDTO, Model model) {
        try {
            // Check if email already exists
            if (usersRepository.existsByEmail(usersDTO.getEmail())) {
                model.addAttribute("error", "This email has already been used by another user.");
                model.addAttribute("positions", positionRepository.findAll());
                model.addAttribute("teams", teamRepository.findAll());
                model.addAttribute("departments", departmentRepository.findAll());
                model.addAttribute("roles", roleRepository.findAll());
                return "registration"; // Return to registration page with error
            }
    
            PositionEntity position = positionRepository.findById(usersDTO.getPosition().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Position ID"));
            TeamEntity team = teamRepository.findById(usersDTO.getTeam().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Team ID"));
            DepartmentEntity department = departmentRepository.findById(usersDTO.getDepartment().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Department ID"));
            RoleEntity role = roleRepository.findById(usersDTO.getRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Role ID"));
    
            UsersEntity user = modelMapper.map(usersDTO, UsersEntity.class);
            user.setPosition(position);
            user.setTeam(team);
            user.setDepartment(department);
            user.setRole(role);
    
            usersRepository.save(user);
    
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("teams", teamRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "registration";
        }
    
        return "registration_success";
    }
    

    public String showRegistrationListPage(Model model) {
        List<UsersEntity> userEntities = usersRepository.findAllByOrderByIdAsc();
        List<UsersDTO> users = userEntities.stream()
            .map(entity -> modelMapper.map(entity, UsersDTO.class))
            .collect(Collectors.toList());
        model.addAttribute("users", users);
        return "registration_list"; // This should match your Thymeleaf template name
    }
    
    // --- UsersService methods merged here ---
    public List<UsersDTO> getAllUsers() {
        return usersRepository.findAll()
                             .stream()
                             .map(user -> modelMapper.map(user, UsersDTO.class))
                             .toList();
    }

    public UsersDTO getUserById(Long id) {
        UsersEntity user = usersRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return modelMapper.map(user, UsersDTO.class);
    }

    public void updateUser(UsersDTO userDTO) {
        UsersEntity userEntity = usersRepository.findById(userDTO.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userEntity.setName(userDTO.getName());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setPhone(userDTO.getPhone());

        if (userDTO.getDepartment() != null && userDTO.getDepartment().getId() != null) {
            DepartmentEntity department = departmentRepository.findById(userDTO.getDepartment().getId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
            userEntity.setDepartment(department);
        }

        if (userDTO.getPosition() != null && userDTO.getPosition().getId() != null) {
            PositionEntity position = positionRepository.findById(userDTO.getPosition().getId())
                .orElseThrow(() -> new IllegalArgumentException("Position not found"));
            userEntity.setPosition(position);
        }

        if (userDTO.getTeam() != null && userDTO.getTeam().getId() != null) {
            TeamEntity team = teamRepository.findById(userDTO.getTeam().getId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
            userEntity.setTeam(team);
        }

        if (userDTO.getRole() != null && userDTO.getRole().getId() != null) {
            RoleEntity role = roleRepository.findById(userDTO.getRole().getId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
            userEntity.setRole(role);
        }

        usersRepository.save(userEntity);
    }


    // User details View or Edit or Delete........................................................
    public Map<String, Object> getUserDetailsOrEdit(Long id, boolean edit) {
        UsersDTO user = getUserById(id);  // Get user details
        List<PositionEntity> positions = positionRepository.findAll();
        List<TeamEntity> teams = teamRepository.findAll();
        List<DepartmentEntity> departments = departmentRepository.findAll();
        List<RoleEntity> roles = roleRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("positions", positions);
        data.put("teams", teams);
        data.put("departments", departments);
        data.put("roles", roles);
        data.put("isEditable", edit);

        return data;
    }

    public void deleteUser(Long id) {
        usersRepository.deleteById(id);  // Deletes the user with the given ID from the database
    }

    
    public UsersDTO getOriginalUserDetails(Long id) {
        UsersEntity userEntity = usersRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return modelMapper.map(userEntity, UsersDTO.class);
    }



    // Searh employee in registration list by name or id...................................................

    public List<UsersDTO> searchUsersByIdOrName(String idQuery, String nameQuery) {
        if (idQuery != null && !idQuery.isEmpty()) {
            try {
                Long id = Long.parseLong(idQuery);
                Optional<UsersEntity> userEntity = usersRepository.findById(id);
                return userEntity.map(entity -> Collections.singletonList(modelMapper.map(entity, UsersDTO.class)))
                        .orElse(Collections.emptyList());
            } catch (NumberFormatException e) {
                // Handle invalid ID format gracefully
                return Collections.emptyList();
            }
        }

        if (nameQuery != null && !nameQuery.isEmpty()) {
            List<UsersEntity> userEntities = usersRepository.findByNameContainingIgnoreCase(nameQuery);
            return userEntities.stream()
                    .map(entity -> modelMapper.map(entity, UsersDTO.class))
                    .collect(Collectors.toList());
        }

        // Return an empty list if both queries are empty
        return Collections.emptyList();
    }

    
}