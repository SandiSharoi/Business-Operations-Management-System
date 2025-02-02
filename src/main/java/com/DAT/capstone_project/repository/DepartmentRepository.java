package com.DAT.capstone_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DAT.capstone_project.model.DepartmentEntity;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Integer>{
    // List<DepartmentEntity> findByName(String name);

    Optional<DepartmentEntity> findByName(String name);

    List<DepartmentEntity> findAllByOrderByIdAsc();
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

}


