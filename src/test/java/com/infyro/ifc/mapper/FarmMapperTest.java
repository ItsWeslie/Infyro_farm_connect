package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import com.infyro.ifc.model.Farm;
import com.infyro.ifc.model.Field;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FarmMapperTest {

    private final FarmMapper farmMapper =
            new FarmMapper(new FieldMapper());

    @Test
    void mapsFarmFieldsToFieldResponseDtos() {
        Field field = Field.builder()
                .id(2L)
                .fieldName("North Field")
                .totalArea(new BigDecimal("10.00"))
                .crops(new ArrayList<>())
                .build();

        field.getCrops().add(CropPlanting.builder()
                .field(field)
                .cropArea(new BigDecimal("3.00"))
                .status(CropPlantingStatus.PLANNED)
                .build());

        Farm farm = Farm.builder()
                .id(1L)
                .farmName("My Farm")
                .fields(List.of(field))
                .build();

        FarmResponse response = farmMapper.mapToFarmResponse(farm);

        assertEquals(1L, response.getId());
        assertEquals("My Farm", response.getName());
        assertEquals(1, response.getFields().size());

        FieldResponse fieldResponse = response.getFields().get(0);

        assertEquals(2L, fieldResponse.getId());
        assertEquals("North Field", fieldResponse.getFieldName());
        assertEquals(
                0,
                new BigDecimal("10.00").compareTo(fieldResponse.getTotalArea()));
        assertEquals(
                0,
                new BigDecimal("7.00").compareTo(fieldResponse.getAvailableArea()));

        assertEquals(FieldResponse.class, fieldResponse.getClass());
    }
}
