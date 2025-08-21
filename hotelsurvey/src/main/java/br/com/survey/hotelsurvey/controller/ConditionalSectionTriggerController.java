package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.ConditionalTriggerRequest;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.service.ConditionalSectionTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triggers")
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
public class ConditionalSectionTriggerController {

    private final ConditionalSectionTriggerService triggerService;

    @GetMapping("/conditional-forms")
    public List<SurveySectionDto> getConditionalForms(
            @RequestParam Long questionId,
            @RequestParam String answerValue,
            @RequestParam String language
    ) {
        return triggerService.getTriggeredSectionsWithTranslations(questionId, answerValue, language);
    }

    @PostMapping
    public ResponseEntity<Void> createTrigger(@RequestBody ConditionalTriggerRequest request) {
        triggerService.createTrigger(request);
        return ResponseEntity.ok().build();
    }
}