package com.infyro.ifc.controller;

import com.infyro.ifc.dto.activity.CropActivityLogResponse;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.CropActivityLogService;
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

class CropActivityOwnershipApiTest {
    private final CropActivityLogService service = mock(CropActivityLogService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CropActivityLogController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void activityNotOwnedByCallerReturnsStructured404() throws Exception {
        when(service.getById(83L, "attacker@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop activity log not found"));

        mockMvc.perform(get("/ifc/v1/crop-activities/83")
                        .principal((Principal) () -> "attacker@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(service).getById(83L, "attacker@example.com");
    }

    @Test
    void activityLookupPassesAuthenticatedPrincipal() throws Exception {
        CropActivityLogResponse response = mock(CropActivityLogResponse.class);
        when(service.getById(83L, "owner@example.com"))
                .thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/ifc/v1/crop-activities/83")
                        .principal((Principal) () -> "owner@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(service).getById(83L, "owner@example.com");
    }
}
