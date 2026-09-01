package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.auth.AuthRequest;
import com.infyro.ifc.dto.auth.AuthResponse;
import com.infyro.ifc.service.AuthValidateService;
import com.infyro.ifc.service.LoginService;
import com.infyro.ifc.service.LogoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class AuthController {

    private final LoginService loginService;
    private final LogoutService logoutService;
    private final AuthValidateService authValidateService;

    @PostMapping(APIConstants.Auth.LOGIN)
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        return loginService.login(authRequest);
    }

    @PostMapping(APIConstants.Auth.LOGOUT)
    public ResponseEntity<APIResponse> logout()
    {
        return logoutService.logout();
    }

    @GetMapping(APIConstants.Auth.VALIDATE)
    public ResponseEntity<AuthResponse> validate(HttpServletRequest request) {
        return authValidateService.validate(request);
    }


}
