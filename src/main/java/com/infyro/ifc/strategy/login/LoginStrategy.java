package com.infyro.ifc.strategy.login;

import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.TokenResponse;
import org.springframework.http.ResponseEntity;

public interface LoginStrategy {
    ResponseEntity<TokenResponse> login(LoginRequest loginRequest);
}