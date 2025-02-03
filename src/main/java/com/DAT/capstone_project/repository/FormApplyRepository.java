package com.DAT.capstone_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.DAT.capstone_project.model.FormApplyEntity;
import com.DAT.capstone_project.model.UsersEntity;


@Repository
public interface FormApplyRepository extends JpaRepository<FormApplyEntity, Long> {
    // Fetch forms for a specific employee, sorted by formApplyId in ascending order
    List<FormApplyEntity> findByEmployeeOrderByFormApplyIdAsc(UsersEntity employee);
}