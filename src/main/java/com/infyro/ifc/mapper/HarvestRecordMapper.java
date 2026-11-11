package com.infyro.ifc.mapper;

import com.infyro.ifc.dto.harvest.HarvestRecordResponse;
import com.infyro.ifc.model.HarvestRecord;
import org.springframework.stereotype.Component;

@Component
public class HarvestRecordMapper {
    public HarvestRecordResponse response(HarvestRecord record) {
        return new HarvestRecordResponse(record.getId(), record.getCropPlanting().getId(),
                record.getHarvestDate(), record.getQuantity(), record.getUnit(),
                record.getNotes(), record.getCreatedAt(), record.getUpdatedAt(),
                record.isVoided(), record.isVoided() ? "Cancelled" : "Active",
                record.getVoidedAt(), record.getVoidReason());
    }
}
