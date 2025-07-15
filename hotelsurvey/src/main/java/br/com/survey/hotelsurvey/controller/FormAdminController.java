package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.service.FormAdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forms")
@PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')") // Admin and regular users can manage forms
public class FormAdminController {

    @Autowired
    private FormAdminService formAdminService;

    @PostMapping
    public ResponseEntity<SurveySectionDto> createForm(@Valid @RequestBody SurveySectionDto dto) {
        SurveySectionDto createdForm = formAdminService.createForm(dto);
        return new ResponseEntity<>(createdForm, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SurveySectionDto> updateForm(@PathVariable Long id, @Valid @RequestBody SurveySectionDto dto) {
        SurveySectionDto updatedForm = formAdminService.updateForm(id, dto);
        return ResponseEntity.ok(updatedForm);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateForm(@PathVariable Long id) {
        formAdminService.deactivateForm(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateForm(@PathVariable Long id) {
        formAdminService.activateForm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SurveySectionDto> getFormById(@PathVariable Long id) {
        SurveySectionDto form = formAdminService.getFormById(id);
        return ResponseEntity.ok(form);
    }

    @GetMapping
    public ResponseEntity<List<SurveySectionDto>> getAllForms() {
        List<SurveySectionDto> forms = formAdminService.getAllForms();
        return ResponseEntity.ok(forms);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<SurveySectionDto> previewForm(@PathVariable Long id) {
        SurveySectionDto preview = formAdminService.previewForm(id);
        return ResponseEntity.ok(preview);
    }
}
