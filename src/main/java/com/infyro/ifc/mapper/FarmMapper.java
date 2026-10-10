package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.farm.FarmResponse;
import com.infyro.ifc.model.Farm;
import org.springframework.stereotype.Component;


@Component
public class FarmMapper {

    public FarmResponse mapToFarmResponse(Farm farm){

        return FarmResponse.builder()
                .id(farm.getId())
                .name(farm.getFarmName())
                .owner(farm.getOwner())
                .fields(farm.getFields())
                .build();
    }
}

