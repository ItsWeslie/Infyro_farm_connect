package com.infyro.ifc.exception;

import com.infyro.ifc.dto.ApiErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void responseStatusExceptionPreservesStatusAndUsesStructuredBody() {
        ResponseEntity<ApiErrorResponse> response = handler.handleResponseStatusException(
                new ResponseStatusException(HttpStatus.CONFLICT, "Record already cancelled"));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("CONFLICT", response.getBody().code());
        assertEquals("Record already cancelled", response.getBody().message());
        assertEquals(0, response.getBody().fieldErrors().size());
    }

    @Test
    void validationFailureIncludesFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new ValidationRequest(), "request");
        bindingResult.addError(new FieldError(
                "request", "amount", null, false, null, null, "must be greater than zero"));
        MethodParameter parameter = new MethodParameter(
                ValidationFixture.class.getDeclaredMethod("submit", ValidationRequest.class), 0);

        ResponseEntity<ApiErrorResponse> response = handler.handleValidationException(
                new MethodArgumentNotValidException(parameter, bindingResult));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals("Request validation failed", response.getBody().message());
        assertEquals(1, response.getBody().fieldErrors().size());
        assertEquals("amount", response.getBody().fieldErrors().getFirst().field());
        assertEquals("must be greater than zero", response.getBody().fieldErrors().getFirst().message());
    }

    @Test
    void unexpectedExceptionDoesNotExposeInternalMessage() {
        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpectedException(
                new IllegalStateException("database password must not leak"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().code());
        assertEquals("An unexpected error occurred", response.getBody().message());
    }

    static class ValidationRequest {
        public String amount;
    }

    static class ValidationFixture {
        void submit(ValidationRequest request) {
        }
    }
}
