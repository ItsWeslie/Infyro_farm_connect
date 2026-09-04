package com.infyro.ifc.dto.auth;

import com.infyro.ifc.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String username;
    private UserType role;
    private String message;
    private boolean isLoggedIn;
}
