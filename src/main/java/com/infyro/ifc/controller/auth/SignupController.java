package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.auth.SignupRequest;
import com.infyro.ifc.service.signup.SignupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.AUTH)
public class SignupController {

    private final SignupService signupService;

    @PostMapping(APIConstants.Auth.SIGNUP)
    public ResponseEntity<APIResponse> signup(@Valid @RequestBody SignupRequest signupRequest) {
        return signupService.signup(signupRequest);
    }

}
