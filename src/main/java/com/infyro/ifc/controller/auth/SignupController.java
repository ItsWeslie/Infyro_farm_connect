package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.auth.SignupRequest;
import com.infyro.ifc.service.signup.SignupService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class SignupController {

    private final SignupService signupService;

    @PostMapping(APIConstants.Auth.SIGNUP)
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request,
                                              HttpServletRequest servletRequest) {
        return signupService.signup(request, servletRequest);
    }
}