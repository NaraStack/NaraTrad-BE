package com.naratrad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for password change request.
 * Used in the PUT /api/profile/password endpoint
 * Requires old password for security validation.
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "The old password cannot be empty")
    private String oldPassword;

    @NotBlank(message = "The new password cannot be empty")
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;
}