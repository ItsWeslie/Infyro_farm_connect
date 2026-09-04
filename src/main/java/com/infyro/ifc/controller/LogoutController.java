package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.service.login.LogoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class LogoutController {
    private final LogoutService logoutService;

    @PostMapping(APIConstants.Auth.LOGOUT)
    public ResponseEntity<APIResponse> logout()
    {
        return logoutService.logout();
    }
}
