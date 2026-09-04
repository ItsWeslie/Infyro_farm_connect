package com.infyro.ifc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "Username must required")
        String name,

        @Email(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "Invalid email format")
        @NotBlank(message = "Email must not be empty")
        String email,

        @Size(min = 8,message = "Password must be at least 8 characters!")
        @NotBlank(message = "Password must required")
        @Pattern(regexp =  "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$",
                message = "Password must be at least 8 characters long and contain at least one uppercase letter, " +
                        "one lowercase letter, one number, and one special character")
        String password
) {
}
