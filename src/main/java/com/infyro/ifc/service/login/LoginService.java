package com.infyro.ifc.service.login;

import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.LoginResponse;
import com.infyro.ifc.strategy.login.LoginStrategy;
import com.infyro.ifc.strategy.login.LoginStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final LoginStrategyFactory loginStrategyFactory;

    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        String loginType = loginRequest.loginType().toLowerCase();
        LoginStrategy loginStrategy = loginStrategyFactory.getStrategy(loginType);
        return loginStrategy.login(loginRequest);
    }
}
