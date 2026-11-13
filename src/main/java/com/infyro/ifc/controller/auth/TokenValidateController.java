package com.infyro.ifc.controller.auth;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.auth.LoginResponse;
import com.infyro.ifc.enums.UserType;
import com.infyro.ifc.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(APIConstants.AUTH)
public class TokenValidateController {

    @GetMapping(APIConstants.Auth.VALIDATE)
    public ResponseEntity<LoginResponse> validate(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(new LoginResponse(principal.getUser().getUsername(), UserType.USER,
                "validated successfully", true));
    }
}