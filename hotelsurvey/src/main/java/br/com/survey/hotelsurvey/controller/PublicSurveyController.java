package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseRequest;
import br.com.survey.hotelsurvey.service.PublicApiService;
import br.com.survey.hotelsurvey.service.SurveyResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/survey")
public class PublicSurveyController {

    @Autowired
    private PublicApiService publicApiService;

    @Autowired
    private SurveyResponseService surveyResponseService;

    @GetMapping("/questions/{formId}/{language}")
    public ResponseEntity<PublicSurveySectionDto> getSurveyQuestions(
            @PathVariable Long formId,
            @PathVariable String language) {

        PublicSurveySectionDto surveySection = publicApiService.getSingleSurveySectionByFormIdAndLanguage(formId, language);
        return ResponseEntity.ok(surveySection);
    }

    /*endpoint que recebe a resposta do hospede, nao precisa de autenticação
     *@param request DTO contendo as respostas do formulário.
     *@return ResponseEntity com o ID da resposta salva ou erro*/
    @PostMapping("/submit-response")
    public ResponseEntity<String> submitSurveyResponse(@Valid @RequestBody SurveyResponseRequest request) {
        try {
            Long responseId = surveyResponseService.submitSurveyResponse(request);
            return new ResponseEntity<>("Survey response submitted successfully with ID: " + responseId, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error submitting survey response: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<SurveyResponseDetailDto> getById(@PathVariable Long id) {
        SurveyResponseDetailDto dto = surveyResponseService.getSurveyResponseById(id);
        return ResponseEntity.ok(dto);
    }






}