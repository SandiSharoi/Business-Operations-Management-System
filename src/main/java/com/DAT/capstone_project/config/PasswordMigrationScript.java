package com.DAT.capstone_project.config;

import com.DAT.capstone_project.model.UsersEntity;
import com.DAT.capstone_project.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordMigrationScript implements CommandLineRunner {

    @Autowired
    private UsersRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Fetch all users
        Iterable<UsersEntity> users = userRepository.findAll();

        for (UsersEntity user : users) {
            // If the password is still in plain text
            if (!isPasswordHashed(user.getPassword())) {
                // Hash the password and update it in the database
                String hashedPassword = passwordEncoder.encode(user.getPassword());
                user.setPassword(hashedPassword);
                userRepository.save(user);
            }
        }
    }

    private boolean isPasswordHashed(String password) {
        // Check if the password is already hashed
        // BCrypt hashes are always 60 characters long
        return password.length() == 60;
    }
}