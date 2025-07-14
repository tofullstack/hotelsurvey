package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.service.PublicApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/survey")
public class PublicSurveyController {

    @Autowired
    private PublicApiService publicApiService;

    @GetMapping("/questions/{companyId}/{language}")
    public ResponseEntity<List<PublicSurveySectionDto>> getSurveyQuestions(
            @PathVariable Long companyId,
            @PathVariable String language) {
            List<PublicSurveySectionDto> surveyQuestions = publicApiService.getSurveyQuestions(companyId, language);
        return ResponseEntity.ok(surveyQuestions);
    }
}
