package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.auth.TokenResponse;
import com.infyro.ifc.security.CsrfCookieValidator;
import com.infyro.ifc.security.RateLimitService;
import com.infyro.ifc.service.login.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(APIConstants.AUTH)
@RequiredArgsConstructor
public class RefreshController {
    private final RefreshTokenService refreshTokens;
    private final CsrfCookieValidator csrf;
    private final RateLimitService rateLimits;

    @PostMapping(APIConstants.Auth.REFRESH)
    public ResponseEntity<TokenResponse> refresh(HttpServletRequest request) {
        csrf.validate(request);
        String ip = request.getRemoteAddr();
        rateLimits.check("refresh:" + ip, 30, 3600);
        return refreshTokens.rotate(request);
    }
}