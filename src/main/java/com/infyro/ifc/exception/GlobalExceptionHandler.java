package com.infyro.ifc.exception;

import com.infyro.ifc.dto.ApiErrorResponse;
import com.infyro.ifc.dto.ApiFieldError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFoundException(UserNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_TOKEN", e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return error(HttpStatus.CONFLICT, "USER_ALREADY_EXISTS", e.getMessage());
    }

    @ExceptionHandler(FarmNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFarmNotFoundException(FarmNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "FARM_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(FieldIsFullWithCropsException.class)
    public ResponseEntity<ApiErrorResponse> handleFieldIsFullWithCropsException(FieldIsFullWithCropsException e) {
        return error(HttpStatus.BAD_REQUEST, "FIELD_FULL", e.getMessage());
    }

    @ExceptionHandler(FieldNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleFieldNotFoundException(FieldNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, "FIELD_NOT_FOUND", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<ApiFieldError> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ApiFieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest().body(new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Request validation failed",
                fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body is missing or malformed");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String code = switch (status) {
            case BAD_REQUEST -> "BAD_REQUEST";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "FORBIDDEN";
            case NOT_FOUND -> "NOT_FOUND";
            case CONFLICT -> "CONFLICT";
            default -> "REQUEST_FAILED";
        };
        String message = e.getReason() == null ? status.getReasonPhrase() : e.getReason();
        return error(status, code, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception e) {
        log.error("Unhandled API exception", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred");
    }

    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                status.value(),
                code,
                message == null || message.isBlank() ? status.getReasonPhrase() : message,
                List.of()));
    }
}
