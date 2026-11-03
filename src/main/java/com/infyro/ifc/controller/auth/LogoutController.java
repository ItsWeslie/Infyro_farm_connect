package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.security.CsrfCookieValidator;
import com.infyro.ifc.service.login.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor @RequestMapping(APIConstants.AUTH)
public class LogoutController {
    private final RefreshTokenService refreshTokens;
    private final CsrfCookieValidator csrf;
    @PostMapping(APIConstants.Auth.LOGOUT)
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        csrf.validate(request); return refreshTokens.logout(request);
    }
    @PostMapping(APIConstants.Auth.LOGOUT_ALL)
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        refreshTokens.logoutAll(authentication.getName()); return ResponseEntity.noContent().build();
    }
}