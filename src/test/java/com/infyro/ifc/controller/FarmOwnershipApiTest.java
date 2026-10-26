package com.infyro.ifc.controller;

import com.infyro.ifc.dto.farm.FarmRequest;
import com.infyro.ifc.exception.FarmNotFoundException;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.FarmService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FarmOwnershipApiTest {

    private final FarmService farmService = mock(FarmService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new FarmController(farmService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void farmNotOwnedByAuthenticatedPrincipalReturnsStructured404() throws Exception {
        when(farmService.getFarmByID(42L, "other-user@example.com"))
                .thenThrow(new FarmNotFoundException("Farm not found with ID: 42"));

        mockMvc.perform(get("/ifc/v1/farm/42")
                        .principal((Principal) () -> "other-user@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("FARM_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Farm not found with ID: 42"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(farmService).getFarmByID(42L, "other-user@example.com");
    }
}
