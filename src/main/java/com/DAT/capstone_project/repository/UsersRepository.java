package com.DAT.capstone_project.repository;

import com.DAT.capstone_project.model.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
    Optional<UsersEntity> findByEmail(String email);
    @SuppressWarnings("null")
    Optional<UsersEntity> findById(Long id);
    Optional<UsersEntity> findByName(String name);

        // Add this method for searching by name
    List<UsersEntity> findByNameContainingIgnoreCase(String name);

    List<UsersEntity> findAllByOrderByIdAsc();

    boolean existsByEmail(String email); // Checks if an email is already in the database

}