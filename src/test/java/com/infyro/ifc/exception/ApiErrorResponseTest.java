package com.infyro.ifc.exception;

import com.infyro.ifc.dto.ApiErrorResponse;
import com.infyro.ifc.dto.ApiFieldError;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiErrorResponseTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void malformedRequestReturnsSafeStructuredBadRequest() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException(
                "Parser internals", new MockHttpInputMessage(
                "{".getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<ApiErrorResponse> response = handler.handleUnreadableMessage(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("MALFORMED_REQUEST", response.getBody().code());
        assertEquals("Request body is missing or malformed", response.getBody().message());
        assertEquals(List.of(), response.getBody().fieldErrors());
    }

    @Test
    void fieldErrorDtoCarriesFieldAndMessage() {
        ApiFieldError fieldError = new ApiFieldError("amount", "must be greater than zero");

        assertEquals("amount", fieldError.field());
        assertEquals("must be greater than zero", fieldError.message());
    }
}
