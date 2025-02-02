package com.DAT.capstone_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.DAT.capstone_project.model.PositionEntity;

@Repository
public interface PositionRepository extends JpaRepository<PositionEntity, Integer>{
    List<PositionEntity> findByName(String name);

}