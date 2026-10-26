package com.infyro.ifc.dto;

import java.util.List;

public record ApiErrorResponse(
        int status,
        String code,
        String message,
        List<ApiFieldError> fieldErrors
) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }
}
