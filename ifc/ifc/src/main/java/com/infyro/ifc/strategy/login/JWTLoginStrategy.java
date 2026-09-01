package com.infyro.ifc.strategy.login;

import com.infyro.ifc.api.LoginTypeConstants;
import com.infyro.ifc.dto.auth.AuthRequest;
import com.infyro.ifc.dto.auth.AuthResponse;
import com.infyro.ifc.enums.UserType;
import com.infyro.ifc.model.User;
import com.infyro.ifc.security.JWTService;
import com.infyro.ifc.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;


@Service(LoginTypeConstants.JWT)
@RequiredArgsConstructor
public class JWTLoginStrategy implements LoginStrategy {

    private final JWTService jwtService;
    private final AuthenticationManager authManager;

    private String getJwtCookie(String token) {
        ResponseCookie jwtCookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(24 * 60 * 60)
                .build();
        return jwtCookie.toString();
    }

    @Override
    public ResponseEntity<AuthResponse> login(AuthRequest authRequest) {

        Authentication auth = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        authRequest.email(),
                        authRequest.password()
                        ));

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        if(principal == null) {
            throw new RuntimeException("Principal is null");
        }
        User user = principal.getUser();

        String token = jwtService.generateToken(user.getEmail());

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, getJwtCookie(token))
                .body(
                     AuthResponse.builder()
                             .username(user.getEmail())
                             .role(UserType.USER)
                             .message("Login successful")
                             .isLoggedIn(true)
                             .build()
                );
    }
}
