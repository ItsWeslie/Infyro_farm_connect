package com.infyro.ifc.service.signup;

import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.auth.SignupRequest;
import com.infyro.ifc.exception.UserAlreadyExistsException;
import com.infyro.ifc.model.User;
import com.infyro.ifc.repository.UserRepo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignupService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseEntity<APIResponse> signup(@Valid SignupRequest signupRequest) {

        boolean isUserExists = userRepo.existsByEmail(signupRequest.email());

        if (isUserExists) {
            log.warn("Signup attempted with an email that already exists");
            throw new UserAlreadyExistsException(
                    "User already exists with this email id: " + signupRequest.email());
        }

        String hashedPassword = passwordEncoder.encode(signupRequest.password());

        User newUser = User.builder()
                .username(signupRequest.name())
                .email(signupRequest.email())
                .password(hashedPassword)
                .build();

        try {
            userRepo.save(newUser);
        } catch (DataIntegrityViolationException e) {

            log.warn("User signup failed because email already exists: {}",
                    signupRequest.email());

            throw new UserAlreadyExistsException(
                    "User already exists with this email id: "
                            + signupRequest.email());
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new APIResponse("User registered successfully"));

    }
}
