package com.infyro.ifc.controller;

import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.exception.GlobalExceptionHandler;
import com.infyro.ifc.service.CropActivityLogService;
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

class CropActivityMutationOwnershipApiTest {
    private final CropActivityLogService service = mock(CropActivityLogService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new CropActivityLogController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void statusChangeForActivityNotOwnedByCallerReturns404WithoutMutation() throws Exception {
        when(service.updateStatus(83L, "attacker@example.com", ActivityStatus.COMPLETED))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop activity log not found"));

        mockMvc.perform(patch("/ifc/v1/crop-activities/83/status")
                        .param("status", "COMPLETED")
                        .principal((Principal) () -> "attacker@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(service).updateStatus(83L, "attacker@example.com", ActivityStatus.COMPLETED);
        verifyNoMoreInteractions(service);
    }

    @Test
    void deleteForActivityNotOwnedByCallerReturns404WithoutMutation() throws Exception {
        when(service.delete(83L, "attacker@example.com"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop activity log not found"));

        mockMvc.perform(delete("/ifc/v1/crop-activities/83")
                        .principal((Principal) () -> "attacker@example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        verify(service).delete(83L, "attacker@example.com");
        verifyNoMoreInteractions(service);
    }
}
