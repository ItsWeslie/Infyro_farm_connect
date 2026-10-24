
package com.infyro.ifc.service;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.mapper.CropPlantingMapper;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepository;
import com.infyro.ifc.repository.FieldRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CropPlantingServiceTest {

    private static final Long PLANTING_ID = 1L;
    private static final Long FIELD_ID = 2L;
    private static final String EMAIL = "farmer@example.com";

    @Mock
    private CropPlantingRepo plantingRepository;

    @Mock
    private CropTypeRepository cropTypeRepository;

    @Mock
    private FieldRepo fieldRepo;

    @Mock
    private CropPlantingMapper cropPlantingMapper;

    @InjectMocks
    private CropPlantingService service;

    private Field field;
    private CropPlanting planting;

    @BeforeEach
    void setUp() {
        field = Field.builder()
                .id(FIELD_ID)
                .totalArea(new BigDecimal("10.00"))
                .availableArea(new BigDecimal("7.00"))
                .build();

        planting = CropPlanting.builder()
                .id(PLANTING_ID)
                .field(field)
                .cropArea(new BigDecimal("3.00"))
                .maturityTimeInDays(90)
                .status(CropPlantingStatus.PLANNED)
                .build();

        when(plantingRepository.findOwnedPlantingForUpdate(
                PLANTING_ID, EMAIL))
                .thenReturn(Optional.of(planting));
    }

    @Test
    void cancellingPlantingRestoresAreaExactlyOnce() {
        when(fieldRepo.findOwnedFieldForUpdate(FIELD_ID, EMAIL))
                .thenReturn(Optional.of(field));

        when(plantingRepository.save(any(CropPlanting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(
                PLANTING_ID, EMAIL, CropPlantingStatus.CANCELLED);

        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(field.getAvailableArea()));

        assertEquals(CropPlantingStatus.CANCELLED, planting.getStatus());

        // A second terminal update must fail without restoring area again.
        assertThrows(ResponseStatusException.class,
                () -> service.updateStatus(
                        PLANTING_ID, EMAIL, CropPlantingStatus.CANCELLED));

        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(field.getAvailableArea()));

        verify(fieldRepo, times(1))
                .findOwnedFieldForUpdate(FIELD_ID, EMAIL);
    }

    @Test
    void completingHarvestedPlantingRestoresAreaAndSetsCompletionDate() {
        planting.setStatus(CropPlantingStatus.HARVESTED);

        when(fieldRepo.findOwnedFieldForUpdate(FIELD_ID, EMAIL))
                .thenReturn(Optional.of(field));

        when(plantingRepository.save(any(CropPlanting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.updateStatus(
                PLANTING_ID, EMAIL, CropPlantingStatus.COMPLETED);

        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(field.getAvailableArea()));

        assertEquals(CropPlantingStatus.COMPLETED, planting.getStatus());
        assertNotNull(planting.getCompletedOn());
    }

    @Test
    void invalidTransitionDoesNotChangeAvailableArea() {
        // PLANNED -> COMPLETED is not an allowed transition.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.updateStatus(
                        PLANTING_ID, EMAIL, CropPlantingStatus.COMPLETED));

        assertEquals(400, exception.getStatusCode().value());

        assertEquals(
                0,
                new BigDecimal("7.00").compareTo(field.getAvailableArea()));

        verify(fieldRepo, never())
                .findOwnedFieldForUpdate(anyLong(), anyString());

        verify(plantingRepository, never()).save(any(CropPlanting.class));
    }
}
