package com.DAT.capstone_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.DAT.capstone_project.model.DepartmentEntity;
import com.DAT.capstone_project.model.TeamEntity;

@Repository
public interface TeamRepository extends JpaRepository<TeamEntity, Integer>{

    @SuppressWarnings("null")
    List<TeamEntity> findAll();  // Fetch all teams

//    List<TeamEntity> findByName(String name);

    List<TeamEntity> findAllByDepartment(DepartmentEntity department);


    List<TeamEntity> findAllByOrderByIdAsc();

    List<TeamEntity> findByDepartmentId(Integer departmentId);

    // To fetch multiple departments at once
    List<TeamEntity> findByDepartmentIdIn(List<Integer> departmentIds);

    List<TeamEntity> findByIdAndDepartmentId(Integer Id, Integer departmentId);


    // Check if pm, dh, divh are null
//    boolean existsByDepartmentIdAndDhIsNotNull(Integer departmentId);

//    boolean existsByDepartmentIdAndDivhIsNotNull(Integer departmentId);

//    boolean existsByIdAndPmIsNotNull(Integer teamId);

    @Query("SELECT t FROM TeamEntity t WHERE LOWER(t.name) = LOWER(:name) AND t.department.id = :departmentId")
    Optional<TeamEntity> findByNameAndDepartmentId(@Param("name") String name, @Param("departmentId") Integer departmentId);

    boolean existsByDepartmentId(Integer departmentId);

//    Find unique department IDs where specific Approver Roles are unassigned...............................................................................
    //DISTINCT ensures no duplicate department IDs

    //Get Departments where no Project Manager (pm.id IS NULL).
    @Query("SELECT DISTINCT t.department.id FROM TeamEntity t WHERE t.pm.id IS NULL")
    List<Integer> findDepartmentIdsWherePmIsNull();

    //Departments where no Department Head (dh.id IS NULL)
    @Query("SELECT DISTINCT t.department.id FROM TeamEntity t WHERE t.dh.id IS NULL")
    List<Integer> findDepartmentIdsWhereDhIsNull();

    //Departments where no Division Head (divh.id IS NULL)
    @Query("SELECT DISTINCT t.department.id FROM TeamEntity t WHERE t.divh.id IS NULL")
    List<Integer> findDepartmentIdsWhereDivhIsNull();

}