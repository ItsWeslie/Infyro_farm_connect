package com.infyro.ifc.controller;

import com.infyro.ifc.dto.crop.CropPlantingResponse;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.CropPlantingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CropPlantingOwnershipApiTest {
    private final CropPlantingService service = mock(CropPlantingService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CropPlantingController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void plantingNotOwnedByCallerReturnsStructured404() throws Exception {
        when(service.getById(72L, "attacker@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Planting not found"));

        mockMvc.perform(get("/ifc/v1/crop-plantings/72")
                        .principal((Principal) () -> "attacker@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(service).getById(72L, "attacker@example.com");
    }

    @Test
    void plantingLookupPassesAuthenticatedPrincipal() throws Exception {
        CropPlantingResponse response = mock(CropPlantingResponse.class);
        when(service.getById(72L, "owner@example.com"))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ifc/v1/crop-plantings/72")
                        .principal((Principal) () -> "owner@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).getById(72L, "owner@example.com");
    }
}
