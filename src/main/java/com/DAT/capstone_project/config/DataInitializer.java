package com.DAT.capstone_project.config;

import com.DAT.capstone_project.model.*;
import com.DAT.capstone_project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UsersRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        // Insert Roles
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new RoleEntity(null, "Admin"),
                    new RoleEntity(null, "Approver"),
                    new RoleEntity(null, "Employee")
            ));
        }

        // Insert Positions
        if (positionRepository.count() == 0) {
            positionRepository.saveAll(List.of(
                    new PositionEntity(null, "Junior Developer"),
                    new PositionEntity(null, "Senior Developer"),
                    new PositionEntity(null, "Project Manager"),
                    new PositionEntity(null, "Department Head"),
                    new PositionEntity(null, "Division Head")
            ));
        }

        // Insert Department
        if (departmentRepository.count() == 0) {
            DepartmentEntity department = new DepartmentEntity(null, "Local");
            departmentRepository.save(department);
        }

        // Insert Team
        if (teamRepository.count() == 0) {
            DepartmentEntity department = departmentRepository.findAll().get(0);
            teamRepository.saveAll(List.of(new TeamEntity("local team 1", department)));
        }

        // Insert Admin User
        if (userRepository.count() == 0) {
            RoleEntity adminRole = roleRepository.findById(1).orElse(null);
            PositionEntity position = positionRepository.findById(1).orElse(null); //Assign Junior Developer
            DepartmentEntity department = departmentRepository.findAll().get(0);
            TeamEntity team = teamRepository.findAll().get(0);

            if (adminRole != null && position != null) {
                UsersEntity admin = new UsersEntity();
                admin.setName("Admin User");
                admin.setEmail("admin@dat.com");
                String hashedPassword = passwordEncoder.encode("Dat0@123");
                admin.setPassword(hashedPassword);
                admin.setPhone("1234567890");
                admin.setRole(adminRole);
                admin.setPosition(position);
                admin.setDepartment(department);
                admin.setTeam(team);
                userRepository.save(admin);
            }
        }
    }
}