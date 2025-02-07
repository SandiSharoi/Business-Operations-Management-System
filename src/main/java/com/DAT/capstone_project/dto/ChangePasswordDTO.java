package com.DAT.capstone_project.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordDTO {
    @NotEmpty
    private String oldPassword;

    @NotEmpty
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    @NotEmpty
    private String confirmPassword;
}