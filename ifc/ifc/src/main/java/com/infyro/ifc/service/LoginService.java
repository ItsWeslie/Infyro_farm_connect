package com.infyro.ifc.service;

import com.infyro.ifc.dto.auth.AuthRequest;
import com.infyro.ifc.dto.auth.AuthResponse;
import com.infyro.ifc.strategy.login.LoginStrategy;
import com.infyro.ifc.strategy.login.LoginStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginStrategyFactory loginStrategyFactory;

    public ResponseEntity<AuthResponse> login(AuthRequest authRequest) {
        String loginType = authRequest.loginType().toLowerCase();
        LoginStrategy loginStrategy = loginStrategyFactory.getStrategy(loginType);
        return loginStrategy.login(authRequest);
    }
}
