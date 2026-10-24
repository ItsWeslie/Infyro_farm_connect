package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.model.Farm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class FarmMapper {

    private final FieldMapper fieldMapper;

    public FarmResponse mapToFarmResponse(Farm farm) {
        return FarmResponse.builder()
                .id(farm.getId())
                .name(farm.getFarmName())
                .fields(farm.getFields().stream()
                        .map(fieldMapper::mapToFieldResponse)
                        .toList())
                .build();
    }
}

