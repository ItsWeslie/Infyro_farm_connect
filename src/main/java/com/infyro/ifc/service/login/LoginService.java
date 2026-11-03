package com.infyro.ifc.service.login;

import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.TokenResponse;
import com.infyro.ifc.security.RateLimitService;
import com.infyro.ifc.strategy.login.LoginStrategy;
import com.infyro.ifc.strategy.login.LoginStrategyFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginStrategyFactory strategies;
    private final RateLimitService rateLimits;

    public ResponseEntity<TokenResponse> login(LoginRequest request,HttpServletRequest servletRequest) {
        String key="login:"+servletRequest.getRemoteAddr()+":"+request.email().toLowerCase(Locale.ROOT);
        rateLimits.checkAllowed(key,5,900);
        LoginStrategy strategy=strategies.getStrategy(request.loginType().toLowerCase(Locale.ROOT));
        try {
            ResponseEntity<TokenResponse> response=strategy.login(request);
            rateLimits.reset(key);
            return response;
        } catch(AuthenticationException exception) {
            rateLimits.recordFailure(key,900);
            throw exception;
        }
    }
}