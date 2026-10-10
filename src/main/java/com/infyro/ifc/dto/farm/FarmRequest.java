package com.infyro.ifc.dto.farm;

import jakarta.validation.constraints.NotBlank;

public record FarmRequest (
        @NotBlank(message = "Farm name must required") String farmName
) {}
