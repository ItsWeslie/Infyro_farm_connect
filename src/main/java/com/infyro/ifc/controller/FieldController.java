package com.infyro.ifc.controller;

import com.infyro.ifc.api.APIConstants;
import com.infyro.ifc.dto.APIResponse;
import com.infyro.ifc.dto.field.FieldRequest;
import com.infyro.ifc.dto.field.FieldResponse;
import com.infyro.ifc.dto.field.FieldUpdateRequest;
import com.infyro.ifc.service.FieldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping(APIConstants.FIELD)
@Slf4j
@RequiredArgsConstructor
public class FieldController {

    private final FieldService fieldService;

    @PostMapping(APIConstants.Field.CREATE)
    public ResponseEntity<FieldResponse> createField(@PathVariable Long farmId,
            @Valid @RequestBody FieldRequest request, Principal principal) {

        return fieldService.createField(
                farmId, request, principal.getName());
        // this creation endpoint requires drop down menu of farms available in current user
    }

    @GetMapping(APIConstants.Field.GET_FIELDS_BY_FARM)
    public ResponseEntity<List<FieldResponse>> getFieldsByFarm(@PathVariable Long farmId,
                                                               Principal principal) {

        return fieldService.getFieldsByFarm(
                farmId, principal.getName());
    }

    @GetMapping(APIConstants.Field.GET_FIELD_BY_ID)
    public ResponseEntity<FieldResponse> getFieldById(@PathVariable Long fieldId,
                                                      Principal principal) {

        return fieldService.getFieldById(
                fieldId, principal.getName());
    }

    @PutMapping(APIConstants.Field.UPDATE)
    public ResponseEntity<FieldResponse> updateField(@PathVariable Long fieldId,
            @Valid @RequestBody FieldUpdateRequest request, Principal principal) {

        return fieldService.updateField(
                fieldId, request, principal.getName());
    }

    @DeleteMapping(APIConstants.Field.DELETE)
    public ResponseEntity<APIResponse> deleteField(@PathVariable Long fieldId,
                                                   Principal principal) {
        return fieldService.deleteField(
                fieldId, principal.getName());
    }
}
