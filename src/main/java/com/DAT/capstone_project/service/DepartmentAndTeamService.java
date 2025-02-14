package com.DAT.capstone_project.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.modelmapper.ModelMapper;

import com.DAT.capstone_project.dto.DepartmentDTO;
import com.DAT.capstone_project.dto.TeamDTO;
import com.DAT.capstone_project.model.TeamEntity;
import com.DAT.capstone_project.model.DepartmentEntity;
import com.DAT.capstone_project.repository.TeamRepository;
import com.DAT.capstone_project.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentAndTeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Department and Team ........................................................

    public String showdepartmentListPage(Model model) {
        List<DepartmentEntity> departmentEntities = departmentRepository.findAllByOrderByIdAsc();
        List<DepartmentDTO> department = departmentEntities.stream()
                .map(entity -> modelMapper.map(entity, DepartmentDTO.class))
                .collect(Collectors.toList());
        model.addAttribute("department", department);
        return "department_list"; // This should match your Thymeleaf template name
    }

    public String showteamListPage(Model model) {
        List<TeamEntity> teamEntities = teamRepository.findAllByOrderByIdAsc();
        List<TeamDTO> team = teamEntities.stream()
                .map(entity -> modelMapper.map(entity, TeamDTO.class))
                .collect(Collectors.toList());
        List<DepartmentEntity> departmentEntities = departmentRepository.findAllByOrderByIdAsc();
        List<DepartmentDTO> department = departmentEntities.stream()
                .map(entity -> modelMapper.map(entity, DepartmentDTO.class))
                .collect(Collectors.toList());
        model.addAttribute("departments", department);
        model.addAttribute("team", team);
        model.addAttribute("teamObject", new TeamEntity());
        return "team_list"; // This should match your Thymeleaf template name
    }

    public DepartmentDTO getDepartmentById(Integer id) {
        // Fetch the department by ID
        DepartmentEntity departmentEntity = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        // Map the entity to DTO (ensure modelMapper is properly configured)
        return modelMapper.map(departmentEntity, DepartmentDTO.class);
    }

    public boolean isDuplicateDepartmentNameOnUpdate(DepartmentDTO departmentDTO) {
        // Check if a department with the same name exists but has a different ID
        return departmentRepository.existsByNameIgnoreCaseAndIdNot(departmentDTO.getName(), departmentDTO.getId());
    }

    public void updateDepartment(DepartmentDTO departmentDTO) {
        if (departmentDTO.getId() == null) {
            throw new IllegalArgumentException("Department ID must not be null");
        }

        // Retrieve the existing department by ID
        DepartmentEntity existingDepartment = departmentRepository.findById(departmentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        // Update only the name field
        existingDepartment.setName(departmentDTO.getName());

        // Save the updated entity
        departmentRepository.save(existingDepartment);
    }

    public boolean isDepartmentNameDuplicate(String departmentName) {
        return departmentRepository.existsByNameIgnoreCase(departmentName);
    }

    public Optional<TeamEntity> isTeamNameUpdate(TeamEntity teams) {
        return teamRepository.findByNameAndDepartmentId(teams.getName().toLowerCase(), teams.getDepartment().getId());
    }

    public Optional<TeamEntity> isDuplicateTeamNameOnUpdate(TeamDTO teamDTO) {
        return teamRepository.findByNameAndDepartmentId(teamDTO.getName().toLowerCase(), teamDTO.getDepartmentId());
    }

    public TeamDTO getTeamById(Integer id) {
        TeamEntity teamEntity = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        return modelMapper.map(teamEntity, TeamDTO.class);
    }

    public void updateTeam(TeamDTO teamDTO) {
        if (teamDTO.getId() == null) {
            throw new IllegalArgumentException("Team ID must not be null");
        }

        TeamEntity existingTeam = teamRepository.findById(teamDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        existingTeam.setName(teamDTO.getName());
        DepartmentEntity departmentEntity = new DepartmentEntity();
        departmentEntity.setId(teamDTO.getDepartmentId());
        existingTeam.setDepartment(departmentEntity);

        teamRepository.save(existingTeam);
    }

    public void updateTeam(TeamDTO teamDTO, Integer dID) {
        if (teamDTO.getId() == null) {
            throw new IllegalArgumentException("Team ID must not be null");
        }

        // Fetch the existing team
        TeamEntity existingTeam = teamRepository.findById(teamDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        existingTeam.setName(teamDTO.getName());

        // Fetch the existing DepartmentEntity from the database
        // DepartmentEntity departmentEntity =
        // departmentRepository.findById(teamDTO.getDepartment().getId())
        DepartmentEntity departmentEntity = departmentRepository.findById(teamDTO.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        existingTeam.setDepartment(departmentEntity);

        // Save the updated team
        teamRepository.save(existingTeam);
    }

    public void deleteDepartment(Integer id) {
        departmentRepository.deleteById(id); // Deletes the user with the given ID from the database
    }

    public boolean hasTeamsLinkedToDepartment(Integer departmentId) {
        // Query the team table to check if any teams are linked to the department
        return teamRepository.existsByDepartmentId(departmentId);
    }

}