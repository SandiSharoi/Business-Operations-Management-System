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
    List<TeamEntity> findByName(String name);

    List<TeamEntity> findAllByDepartment(DepartmentEntity department);


    List<TeamEntity> findAllByOrderByIdAsc();

    List<TeamEntity> findByDepartmentId(Integer departmentId);

    @Query(value = "select * from team where name = :name and department_id  = :departId", nativeQuery = true)
    Optional<TeamEntity> findByNameAndDepartmentId(@Param("name") String name, @Param("departId") Integer departId);
}


