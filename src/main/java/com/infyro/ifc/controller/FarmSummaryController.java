package com.infyro.ifc.controller;

import com.infyro.ifc.dto.farm.*;
import com.infyro.ifc.service.FarmSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/farms/{farmId}")
public class FarmSummaryController {
    private final FarmSummaryService service;

    @GetMapping("/summary")
    public FarmSummaryResponse getSummary(@PathVariable Long farmId, Principal principal) {
        return service.getFarmSummary(farmId, principal.getName());
    }

    @GetMapping("/expenses/summary")
    public ExpenseSummaryResponse getExpenseSummary(@PathVariable Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return service.getExpenseSummary(farmId, principal.getName(), from, to);
    }

    @GetMapping("/harvests/summary")
    public HarvestSummaryResponse getHarvestSummary(@PathVariable Long farmId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Principal principal) {
        return service.getHarvestSummary(farmId, principal.getName(), from, to);
    }

    @GetMapping("/plantings/summary")
    public PlantingSummaryResponse getPlantingSummary(@PathVariable Long farmId, Principal principal) {
        return service.getPlantingSummary(farmId, principal.getName());
    }
}
