package com.infyro.ifc.service;

import com.infyro.ifc.dto.crop.CropPlantingRequest;
import com.infyro.ifc.mapper.CropPlantingMapper;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.CropType;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import com.infyro.ifc.repository.CropPlantingRepo;
import com.infyro.ifc.repository.CropTypeRepository;
import com.infyro.ifc.repository.FieldRepo;
import com.infyro.ifc.enums.CropPlantingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CropPlantingAreaValidationTest {
    @Mock CropPlantingRepo plantingRepository;
    @Mock CropTypeRepository cropTypeRepository;
    @Mock FieldRepo fieldRepo;
    @Mock CropPlantingMapper mapper;
    @InjectMocks CropPlantingService service;

    @Test void rejectsPlantingThatExceedsDerivedAvailableArea() {
        Farm farm = Farm.builder().id(7L).build();
        Field field = Field.builder().id(9L).farm(farm).totalArea(new BigDecimal("10.00")).crops(new ArrayList<>()).build();
        field.getCrops().add(CropPlanting.builder().id(12L).field(field).cropArea(new BigDecimal("3.00")).plantedOn(LocalDate.of(2026, 1, 1)).maturityTimeInDays(40).status(CropPlantingStatus.PLANNED).build());
        CropType type = CropType.builder().id(4L).farm(farm).defaultMaturityTimeInDays(40).build();
        when(fieldRepo.findOwnedFieldForUpdate(9L, "owner@example.com")).thenReturn(Optional.of(field));
        when(cropTypeRepository.findByIdAndFarm_IdAndFarm_Owner_Email(4L, 7L, "owner@example.com")).thenReturn(Optional.of(type));
        CropPlantingRequest request = new CropPlantingRequest(4L, new BigDecimal("8.00"), LocalDate.of(2026, 2, 1), null, 40);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.create(9L, "owner@example.com", request));
        assertEquals(409, error.getStatusCode().value());
        verify(plantingRepository, never()).save(any());
    }
}
