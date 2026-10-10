package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.auth.LoginResponse;
import com.infyro.ifc.service.login.TokenValidateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class TokenValidateController {

    private final TokenValidateService tokenValidateService;

    @GetMapping(APIConstants.Auth.VALIDATE)
    public ResponseEntity<LoginResponse> validate(HttpServletRequest request) {
        return tokenValidateService.validate(request);
    }

}
