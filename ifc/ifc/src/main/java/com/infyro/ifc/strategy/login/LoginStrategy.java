package com.infyro.ifc.strategy.login;

import com.infyro.ifc.dto.auth.AuthRequest;
import com.infyro.ifc.dto.auth.AuthResponse;
import org.springframework.http.ResponseEntity;

public interface LoginStrategy {
    ResponseEntity<AuthResponse> login(AuthRequest authRequest);
}
