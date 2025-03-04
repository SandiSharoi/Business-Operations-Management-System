package com.DAT.capstone_project.service;

import com.DAT.capstone_project.dto.DepartmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


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

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Authentication and Registration ---
    public Optional<UsersEntity> authenticate(String email, String password) {
        Optional<UsersEntity> user = usersRepository.findByEmail(email);

        // Use BCrypt to match the hashed password with the entered password
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        return user.filter(u -> passwordEncoder.matches(password, u.getPassword()));  // Use BCrypt to compare hashed passwords
    }

    public String authenticateAndRedirect(String email, String password, Model model, HttpSession session) {
        Optional<UsersEntity> user = authenticate(email, password);

        if (user.isPresent()) {
            UsersEntity loggedInUser = user.get();

            // Set both loggedInUser and userId in the session
            session.setAttribute("loggedInUser", loggedInUser);  // Store full user object
            session.setAttribute("userId", loggedInUser.getId());  // Store userId for checking in other requests

            model.addAttribute("user", modelMapper.map(loggedInUser, UsersDTO.class));

            // Get the role and position names
            String roleName = loggedInUser.getRole().getName();
            String positionName = loggedInUser.getPosition().getName();

            // Redirect based on role and position
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
                if ("Division Head".equalsIgnoreCase(position)) {
                    model.addAttribute("menu", List.of("Form Decision","Form Decisions History"));
                } else {
                    model.addAttribute("menu", List.of("Form Apply", "Check Form Status", "Form Decision","Form Decisions History"));
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

//Show User Registration Page.........................................................................
    public String showRegistrationPage(Model model) {
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("user", new UsersDTO()); //Creates a new UsersDTO object for form binding.

        return "registration";
    }

    //Show Departments which has pm_id = null or dh_id = null or divh_id = null according to Approver Position Registration...........................................
    public List<DepartmentDTO> getDepartmentsForPosition(String position) {
        List<Integer> departmentIds = new ArrayList<>(); //Initializes an empty departmentIds list

//      Determines which departments to fetch based on position

        //If Project Manager, fetch departments where pm_id is NULL
        if ("Project Manager".equalsIgnoreCase(position)) {
            departmentIds = teamRepository.findDepartmentIdsWherePmIsNull();
        }

        //If Department Head, fetch departments where dh_id is NULL
        else if ("Department Head".equalsIgnoreCase(position)) {
            departmentIds = teamRepository.findDepartmentIdsWhereDhIsNull();
        }

        //If Division Head, fetch departments where divh_id is NULL
        else if ("Division Head".equalsIgnoreCase(position)) {
            departmentIds = teamRepository.findDepartmentIdsWhereDivhIsNull();
        }

        else{
            departmentIds = departmentRepository.findAllDepartmentIds();
        }

        if (!departmentIds.isEmpty()) {

            //Converts departmentIds into a list of DepartmentDTO objects using stream().map()
            return departmentRepository.findByIdIn(departmentIds).stream()
                    .map(dept -> new DepartmentDTO(dept.getId(), dept.getName()))
                    .collect(Collectors.toList());
        }

        //Returns the list or an empty list if no departments match
        return Collections.emptyList(); // Ensures empty list if no valid departments
    }



    @Transactional
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

            // Fetch position
            PositionEntity position = positionRepository.findById(usersDTO.getPosition().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Position ID"));

            String user_position_name = position.getName();

            // Map DTO to Entity
            UsersEntity user = modelMapper.map(usersDTO, UsersEntity.class);
            user.setPassword(passwordEncoder.encode(usersDTO.getPassword()));
            user.setPosition(position);
            user.setRole(roleRepository.findById(usersDTO.getRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Role ID")));


            // Case 1: DH → Only department should be saved, team should be NULL
            if ( user_position_name.equalsIgnoreCase("Department Head")) {
                DepartmentEntity department = departmentRepository.findById(usersDTO.getDepartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Department ID"));



                user.setDepartment(department);
                user.setTeam(null); // Ensure team_id is NULL

                // Find all teams associated with the selected department and save dh_id
                List<TeamEntity> teamsToUpdate = teamRepository.findByDepartmentId(department.getId());
                for (TeamEntity team : teamsToUpdate) {
                    team.setDh(user); // Assign user as Dh in each team
                }
                teamRepository.saveAll(teamsToUpdate); // Save updated teams
            }

            // Case 2: DivH → Team & Department should be NULL, user is assigned to multiple departments
            else if (user_position_name.equalsIgnoreCase("Division Head")) {
                user.setTeam(null);  // Ensure team_id is NULL
                user.setDepartment(null);  // Ensure department_id is NULL

                // Fetch multiple departments selected for DivH
                List<Integer> departmentIds = usersDTO.getDepartmentIds();
                if (departmentIds == null || departmentIds.isEmpty()) {
                    throw new IllegalArgumentException("At least one department must be selected for DivH.");
                }

                // Save user first before updating teams (prevents TransientObjectException)
                user = usersRepository.save(user);

                // Find all teams associated with the selected departments and save divh_id
                List<TeamEntity> teamsToUpdate = teamRepository.findByDepartmentIdIn(departmentIds);
                for (TeamEntity team : teamsToUpdate) {
                    team.setDivh(user); // Assign user as DivH in each team
                }
                teamRepository.saveAll(teamsToUpdate); // Save updated teams
            }

            // Case 3: Other positions → Save both Team & Department
            else {
                TeamEntity team = teamRepository.findById(usersDTO.getTeam().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Team ID"));
                DepartmentEntity department = departmentRepository.findById(usersDTO.getDepartment().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid Department ID"));
                user.setTeam(team);
                user.setDepartment(department);

                if (user_position_name.equalsIgnoreCase("Project Manager")) {

                    // Fetch all teams associated with the specified team ID and department ID
                    List<TeamEntity> teamsToUpdate = teamRepository.findByIdAndDepartmentId(team.getId(), department.getId());
                    for (TeamEntity t : teamsToUpdate) {
                        t.setPm(user); // Assign user as PM in each team
                    }
                    teamRepository.saveAll(teamsToUpdate); // Save updated teams
                }
            }

            // Save user (if not DivH, as it's already saved earlier)
            if (!user_position_name.equalsIgnoreCase("Division Head")) {
                usersRepository.save(user);
            }

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("positions", positionRepository.findAll());
            model.addAttribute("teams", teamRepository.findAll());
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "registration"; // Return to registration page with error
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

    public boolean isEmailTaken(String email, Long userId) {
        // Check if the email already exists for any user other than the current one
        return usersRepository.existsByEmailAndIdNot(email, userId);
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
//    public Map<String, Object> getUserDetailsOrEdit(Long id, boolean edit) {
//        UsersDTO user = getUserById(id);  // Get user details
//        List<PositionEntity> positions = positionRepository.findAll();
//        List<TeamEntity> teams = teamRepository.findAll();
//        List<RoleEntity> roles = roleRepository.findAll();
//
//        Map<String, Object> data = new HashMap<>();
//        data.put("user", user);
//        data.put("positions", positions);
//        data.put("teams", teams);
//        data.put("roles", roles);
//        data.put("isEditable", edit);
//
//        // ✅ If user is a Division Head, fetch associated departments
//        if (user.getPosition() != null && "Division Head".equals(user.getPosition().getName())) {
//            List<Integer> departmentIds = teamRepository.findDepartmentIdsByDivhId(user.getId());
//            user.setDepartmentIds(departmentIds);
//            data.put("departmentIds", departmentIds);  // Add departmentIds to data
//        } else {
//            List<DepartmentEntity> department = departmentRepository.findAll();
//            data.put("department", department);
//        }
//
//        return data;
//    }

    // User details View or Edit or Delete........................................................

    public Map<String, Object> getUserDetailsView(Long id, boolean edit) {
        UsersDTO user = getUserById(id); // Fetch user details from DB
        List<PositionEntity> positions = positionRepository.findAll();
        List<TeamEntity> teams = teamRepository.findAll();
        List<RoleEntity> roles = roleRepository.findAll();


        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("positions", positions);
        data.put("teams", teams);
        data.put("roles", roles);

                // ✅ If user is a Division Head, fetch associated departments
        if (user.getPosition() != null && "Division Head".equals(user.getPosition().getName())) {
            List<Integer> departmentIds = teamRepository.findDepartmentIdsByDivhId(user.getId());
            user.setDepartmentIds(departmentIds);
            data.put("departmentIds", departmentIds);  // Add departmentIds to data
        } else {
            List<DepartmentEntity> department = departmentRepository.findAll();
            data.put("department", department);
        }

        return data;
    }

    public Map<String, Object> getUserDetailsEdit(Long id, boolean edit) {
        UsersDTO user = getUserById(id); // Fetch user details from DB
        List<PositionEntity> positions = positionRepository.findAll();
        List<RoleEntity> roles = roleRepository.findAll();

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("positions", positions);
        data.put("roles", roles);

        List<Integer> unassignedDepartmentIds = new ArrayList<>();
        List<Integer> userDepartmentIds = new ArrayList<>();
        List<Integer> assignedDepartmentIds = new ArrayList<>();
        List<DepartmentEntity> allDepartments = new ArrayList<>();

        if (user.getDepartment() != null) {
            userDepartmentIds.add(user.getDepartment().getId());
        } else if (user.getDepartmentIds() != null) {
            userDepartmentIds.addAll(user.getDepartmentIds());
        }

        // Fetch unassigned and assigned departments for Division Head
        boolean isPm = "Project Manager".equals(user.getPosition().getName());
        boolean isDh = "Department Head".equals(user.getPosition().getName());
        boolean isDivh = "Division Head".equals(user.getPosition().getName());

        if (user.getPosition() != null) {
            if (isPm) {
                unassignedDepartmentIds = teamRepository.findDepartmentIdsWherePmIsNull();
            } else if (isDh) {
                unassignedDepartmentIds = teamRepository.findDepartmentIdsWhereDhIsNull();
            } else if (isDivh) {
                unassignedDepartmentIds = teamRepository.findDepartmentIdsWhereDivhIsNull();
                assignedDepartmentIds = teamRepository.findDepartmentIdsByDivhId(user.getId()); // Assigned departments

                userDepartmentIds.addAll(assignedDepartmentIds);
            } else {
                // Fetch all departments for positions that are neither PM, DH, nor DivH
                allDepartments = departmentRepository.findAllByOrderByIdAsc();
            }
        }

        // Combine department IDs to ensure original department(s) are included
        Set<Integer> finalDepartmentIds = new HashSet<>(unassignedDepartmentIds);
        finalDepartmentIds.addAll(userDepartmentIds);

        if (allDepartments.isEmpty()) { // Fetch departments if not already retrieved
            allDepartments = departmentRepository.findByIdIn(new ArrayList<>(finalDepartmentIds));
        } else {
            // If all departments were fetched, ensure we use their IDs to fetch teams
            finalDepartmentIds.clear();
            for (DepartmentEntity dept : allDepartments) {
                finalDepartmentIds.add(dept.getId());
            }
        }

        data.put("departments", allDepartments);
        data.put("assignedDepartmentIds", assignedDepartmentIds); // Pass assigned departments explicitly


        // Fetch teams for the selected department(s)
        List<TeamEntity> teams = new ArrayList<>();
        TeamEntity assignedTeam = null; // To store the currently assigned team for the PM

        for (Integer deptId : finalDepartmentIds) {
            if (isPm) {
                // Fetch the PM's currently assigned team (if any) for the department
                assignedTeam = teamRepository.findTeamByPmIdAndDeptId(user.getId(), deptId);

                // Only fetch unassigned teams in the selected department (with pm_id = null)
                List<TeamEntity> unassignedTeams = teamRepository.findTeamsWithUnassignedPm(deptId);

                // Add the assigned team first (if exists), then add unassigned teams
                if (assignedTeam != null) {
                    teams.add(assignedTeam); // Highlight this in the dropdown
                }
                teams.addAll(unassignedTeams); // Add unassigned teams to the list for this department
            } else if (isDh) {
                teams.addAll(teamRepository.findTeamsWithUnassignedDh(deptId));
            } else if (isDivh) {
                teams.addAll(teamRepository.findTeamsWithUnassignedDivh(deptId));
            } else {
                // Fetch all teams under the department for other positions
                teams.addAll(teamRepository.findByDepartmentId(deptId));
            }
        }
        data.put("teams", teams);
        data.put("assignedTeam", assignedTeam); // Add the assigned team to the data map

        return data;
    }






    public void deleteUser(Long id) {
        // Fetch all the teams where the user is referenced as PM, DH, or DivH
        List<TeamEntity> teams = teamRepository.findAll();

        for (TeamEntity team : teams) {
            if (team.getPm() != null && team.getPm().getId().equals(id)) {
                team.setPm(null);  // Remove the PM reference
            }
            if (team.getDh() != null && team.getDh().getId().equals(id)) {
                team.setDh(null);  // Remove the DH reference
            }
            if (team.getDivh() != null && team.getDivh().getId().equals(id)) {
                team.setDivh(null);  // Remove the DivH reference
            }
        }

        // After updating the references, save the changes
        teamRepository.saveAll(teams);

        // Now, delete the user from the users table
        usersRepository.deleteById(id);  // Deletes the user with the given ID
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

    // Password Change..........................................................................................

    public String validatePasswordChange(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        UsersEntity user = usersRepository.findById(userId).orElse(null);

        if (user == null) {
            return "User not found.";
        }

        // Check if old password is correct
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return "Old password is incorrect.";
        }

        // Check if new password matches confirm password
        if (!newPassword.equals(confirmPassword)) {
            return "New password and confirm password do not match.";
        }

        // Check if new password meets the required criteria
        if (!isValidPassword(newPassword)) {
            return "New password must contain at least 8 characters, 1 number, 1 uppercase letter, and 1 special character.";
        }

        return null;
    }

    public boolean changePassword(Long userId, String newPassword) {
        UsersEntity user = usersRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // Encrypt the new password and save it
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        return true;
    }

    private boolean isValidPassword(String password) {
        String passwordPattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$";
        return password.matches(passwordPattern);
    }
}