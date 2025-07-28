
package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.ConditionalTriggerRequest;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.ConditionalSectionTrigger;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.mapper.SurveySectionMapper;
import br.com.survey.hotelsurvey.service.ConditionalSectionTriggerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/triggers")
@RequiredArgsConstructor
public class ConditionalSectionTriggerController {

    private final ConditionalSectionTriggerService triggerService;

//    @GetMapping("/question/{questionId}/value/{answerValue}")
//    public List<Long> getTriggeredSectionIds(
//            @PathVariable Long questionId,
//            @PathVariable String answerValue
//    ) {
//        List<ConditionalSectionTrigger> matchingTriggers = triggerService.findTriggersMatching(questionId, answerValue);
//
//        // retorna apenas os IDs das seções que devem ser exibidas
//        return matchingTriggers.stream()
//                .map(trigger -> trigger.getTargetSection().getId())
//                .collect(Collectors.toList());
//    }
//
    // para retorno completo d DTO

    @GetMapping("/question/{questionId}/value/{answerValue}")
    public List<SurveySectionDto> getTriggeredSections(
            @PathVariable Long questionId,
            @PathVariable String answerValue
    ) {
        List<ConditionalSectionTrigger> matchingTriggers = triggerService.findTriggersMatching(questionId, answerValue);

        return matchingTriggers.stream()
                .map(trigger -> SurveySectionMapper.toDto(trigger.getTargetSection()))
                .collect(Collectors.toList());

    }

    @PostMapping
    public ResponseEntity<Void> createTrigger(@RequestBody ConditionalTriggerRequest request) {
        triggerService.createTrigger(request);
        return ResponseEntity.ok().build();
    }


}

