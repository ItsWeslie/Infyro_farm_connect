package com.infyro.ifc.controller.farm;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.farm.*;
import com.infyro.ifc.service.farm.FarmSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping(APIConstants.SUMMARY)
public class FarmSummaryController {

    private final FarmSummaryService service;

    @GetMapping(APIConstants.Summary.OVERALL_SUMMARY)
    public FarmSummaryResponse getSummary(@PathVariable Long farmId, Principal principal) {
        return service.getFarmSummary(farmId, principal.getName());
    }

    @GetMapping(APIConstants.Summary.EXPENSE_SUMMARY)
    public ExpenseSummaryResponse getExpenseSummary(@PathVariable Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return service.getExpenseSummary(farmId, principal.getName(), from, to);
    }

    @GetMapping(APIConstants.Summary.HARVEST_SUMMARY)
    public HarvestSummaryResponse getHarvestSummary(@PathVariable Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return service.getHarvestSummary(farmId, principal.getName(), from, to);
    }

    @GetMapping(APIConstants.Summary.PLANTING_SUMMARY)
    public PlantingSummaryResponse getPlantingSummary(@PathVariable Long farmId, Principal principal) {
        return service.getPlantingSummary(farmId, principal.getName());
    }
}
