package com.infyro.ifc.strategy.login;

import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.LoginResponse;
import org.springframework.http.ResponseEntity;

public interface LoginStrategy {
    ResponseEntity<LoginResponse> login(LoginRequest loginRequest);
}
