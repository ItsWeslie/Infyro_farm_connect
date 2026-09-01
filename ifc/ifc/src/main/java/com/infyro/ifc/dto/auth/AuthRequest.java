package com.infyro.ifc.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "User name must not be empty")
        String username,
        @NotBlank(message = "Password must not be empty")
        String password,
        @NotBlank(message = "Login type must required")
        String loginType
)
{}
