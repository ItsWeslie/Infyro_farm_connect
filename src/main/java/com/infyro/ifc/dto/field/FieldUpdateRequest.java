package com.infyro.ifc.dto.field;

import jakarta.validation.constraints.NotBlank;

public record FieldUpdateRequest(
        @NotBlank(message = "Field name is required")
        String fieldName
) {}
