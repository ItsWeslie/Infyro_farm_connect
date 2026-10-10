package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.auth.LoginRequest;
import com.infyro.ifc.dto.auth.LoginResponse;
import com.infyro.ifc.service.login.LoginService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class LoginController {

    private final LoginService loginService;



    @PostMapping(APIConstants.Auth.LOGIN)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        return loginService.login(loginRequest);
    }




}
