package com.infyro.ifc.controller;

import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.HarvestRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HarvestRecordOwnershipApiTest {
    private final HarvestRecordService service = mock(HarvestRecordService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new HarvestRecordController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void harvestNotOwnedByCallerReturnsStructured404() throws Exception {
        when(service.getById(91L, "attacker@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Harvest record not found"));

        mockMvc.perform(get("/api/crop-plantings/4/harvest-records/91")
                        .principal((Principal) () -> "attacker@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(service).getById(91L, "attacker@example.com");
    }

    @Test
    void harvestFromDifferentPlantingIsHiddenAs404() throws Exception {
        HarvestRecordResponse record = mock(HarvestRecordResponse.class);
        when(record.cropPlantingId()).thenReturn(20L);
        when(service.getById(91L, "owner@example.com")).thenReturn(record);

        mockMvc.perform(get("/api/crop-plantings/4/harvest-records/91")
                        .principal((Principal) () -> "owner@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(service, never()).delete(anyLong(), anyString());
    }
}
