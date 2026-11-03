package com.infyro.ifc.strategy.login;

import com.infyro.ifc.api.LoginTypeConstants;
import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.TokenResponse;
import com.infyro.ifc.model.User;
import com.infyro.ifc.security.UserPrincipal;
import com.infyro.ifc.service.login.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service(LoginTypeConstants.JWT)
@RequiredArgsConstructor
public class JWTLoginStrategy implements LoginStrategy {
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokens;

    @Override
    public ResponseEntity<TokenResponse> login(LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        return refreshTokens.issue(user);
    }
}