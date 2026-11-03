package com.infyro.ifc.service.signup;

import com.infyro.ifc.dto.api.ApiResponse;
import com.infyro.ifc.dto.auth.SignupRequest;
import com.infyro.ifc.exception.UserAlreadyExistsException;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.UserRepo;
import com.infyro.ifc.security.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimits;

    @Transactional
    public ResponseEntity<ApiResponse> signup(@Valid SignupRequest request, HttpServletRequest servletRequest) {
        rateLimits.check("signup:" + servletRequest.getRemoteAddr(), 5, 3600);
        if (userRepo.existsByEmail(request.email()))
            throw new UserAlreadyExistsException("User already exists with this email address");
        User user = User.builder().username(request.name()).email(request.email())
                .password(passwordEncoder.encode(request.password())).build();
        try { userRepo.save(user); }
        catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException("User already exists with this email address");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse("User registered successfully"));
    }
}