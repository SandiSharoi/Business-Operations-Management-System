package com.DAT.capstone_project.repository;

import com.DAT.capstone_project.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findById(Long id);


    Optional<UsersEntity> findByStaffId(String staffId);

    // Add this method for searching by name
    List<UsersEntity> findByNameContainingIgnoreCase(String name);

    List<UsersEntity> findAllByOrderByIdAsc();

    boolean existsByEmail(String email); // Checks if an email is already in the database

    boolean existsByEmailAndIdNot(String email, Long userId);

    boolean existsByStaffId(String staffId);

    boolean existsByStaffIdAndIdNot(String staffId, Long userId);
}