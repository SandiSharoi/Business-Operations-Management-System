package com.DAT.capstone_project.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.DAT.capstone_project.model.FormApplyEntity;
import com.DAT.capstone_project.model.UsersEntity;


@Repository
public interface FormApplyRepository extends JpaRepository<FormApplyEntity, Long> {

    // Fetch forms for a specific employee, sorted by formApplyId in ascending order
    List<FormApplyEntity> findByEmployeeOrderByFormApplyIdAsc(UsersEntity employee);

    List<FormApplyEntity> findByFinalFormStatusIn(List<String> statuses);

    @Query("SELECT COUNT(f) > 0 FROM FormApplyEntity f WHERE f.projectId = :projectId")
    boolean existsByProjectId(@Param("projectId") String projectId);

    boolean existsByProjectIdAndFormApplyIdNot(String projectId, Long formApplyId);

    FormApplyEntity findByFormApplyId(Long formApplyId);
}