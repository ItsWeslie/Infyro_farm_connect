package com.infyro.ifc.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Required valid email address")
        String email,
        @NotBlank(message = "Password must not be empty")
        String password,
        @NotBlank(message = "Login type must required")
        String loginType
)
{}
