package com.DAT.capstone_project.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePasswordResponse {
    // Getters and Setters
    private boolean success;
    private String message;

    public ChangePasswordResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}