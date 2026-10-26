package com.infyro.ifc.controller;

import com.infyro.ifc.dto.crop.CropPlantingStatusRequest;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.CropPlantingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CropPlantingMutationOwnershipApiTest {
    private final CropPlantingService service = mock(CropPlantingService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CropPlantingController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void statusChangeForPlantingNotOwnedByCallerReturns404WithoutMutation() throws Exception {
        when(service.updateStatus(eq(72L), eq("attacker@example.com"), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop planting not found"));

        mockMvc.perform(patch("/ifc/v1/crop-plantings/72/status")
                        .principal((Principal) () -> "attacker@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(service).updateStatus(72L, "attacker@example.com",
                com.infyro.ifc.enums.CropPlantingStatus.CANCELLED);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteForPlantingNotOwnedByCallerReturns404WithoutMutation() throws Exception {
        when(service.delete(72L, "attacker@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop planting not found"));

        mockMvc.perform(delete("/ifc/v1/crop-plantings/72")
                        .principal((Principal) () -> "attacker@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(service).delete(72L, "attacker@example.com");
        verifyNoMoreInteractions(service);
    }
}
