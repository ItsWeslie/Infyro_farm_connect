package com.infyro.ifc.service;


import com.infyro.ifc.dto.auth.AuthResponse;
import com.infyro.ifc.enums.UserType;
import com.infyro.ifc.exception.InvalidTokenException;
import com.infyro.ifc.exception.UserNotFoundException;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.UserRepo;
import com.infyro.ifc.security.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthValidateService {

    private final JWTService jwtService;
    private final UserRepo userRepo;

    public ResponseEntity<AuthResponse> validate(HttpServletRequest request) {

            String token = extractToken(request);

            String username = jwtService.extractUsername(token);
            if (username == null) {
                throw new InvalidTokenException("Invalid token");
            }

            User user = userRepo.findUserByEmail(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (!jwtService.validateToken(token, user.getEmail())) {
                throw new InvalidTokenException("Token expired or invalid");
            }

            return ResponseEntity.ok(
                    new AuthResponse(
                            user.getUsername(),
                            UserType.USER,
                            "validated successfully",
                            true
                    )
            );
        }

        private String extractToken(HttpServletRequest request) {

            if (request.getCookies() == null) {
                throw new InvalidTokenException("Token not found");
            }

            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }

            throw new InvalidTokenException("Token not found");
        }
}
