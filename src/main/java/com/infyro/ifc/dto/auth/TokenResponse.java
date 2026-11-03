package com.infyro.ifc.dto.auth;

import com.infyro.ifc.enums.UserType;

public record TokenResponse(
        String accessToken,
        long expiresIn,
        String username,
        UserType role,
        String message,
        boolean loggedIn
) {}