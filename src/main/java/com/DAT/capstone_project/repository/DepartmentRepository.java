package com.DAT.capstone_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.DAT.capstone_project.model.DepartmentEntity;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer>{
    // List<DepartmentEntity> findByName(String name);

    Optional<DepartmentEntity> findByName(String name);

    List<DepartmentEntity> findAllByOrderByIdAsc();
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    //Finds departments matching the given list of IDs by returning a list of DepartmentEntity objects
    @Query("SELECT d FROM DepartmentEntity d WHERE d.id IN :ids")
    List<DepartmentEntity> findByIdIn(@Param("ids") List<Integer> ids);

    //   Reterive all department IDs from department table
    @Query("SELECT d.id FROM DepartmentEntity d")
    List<Integer> findAllDepartmentIds();

}