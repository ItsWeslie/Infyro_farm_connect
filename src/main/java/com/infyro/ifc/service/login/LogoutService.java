package com.infyro.ifc.service.login;


import com.infyro.ifc.dto.APIResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {
    public ResponseEntity<APIResponse> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(new APIResponse("Logged out successfully"));
    }
}
