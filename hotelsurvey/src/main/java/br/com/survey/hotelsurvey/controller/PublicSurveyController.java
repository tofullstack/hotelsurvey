package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseRequest;
import br.com.survey.hotelsurvey.service.PublicApiService;
import br.com.survey.hotelsurvey.service.SurveyResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/survey")
public class PublicSurveyController {

    @Autowired
    private PublicApiService publicApiService;

    @Autowired
    private SurveyResponseService surveyResponseService;

    @GetMapping("/questions/{companyId}/{language}")
    public ResponseEntity<List<PublicSurveySectionDto>> getSurveyQuestions(
            @PathVariable Long companyId,
            @PathVariable String language) {
            List<PublicSurveySectionDto> surveyQuestions = publicApiService.getSurveyQuestions(companyId, language);
        return ResponseEntity.ok(surveyQuestions);
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
            // captura exceções gerais para retornar uma mensagem de erro mais atraente
            // as exceções especificas (ValidationException, ResourceNotFoundException)
            // serão tratadas pelo Spring com os Status HTTP corretos (@ResponseStatus).
            return new ResponseEntity<>("Error submitting survey response: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // NOVO ENDPOINT PARA OBTER UMA ÚNICA SEÇÃO POR ID para tratamento do QRCODE no frontend
    @GetMapping("/questions/{companyId}/{language}/{sectionId}")
    public ResponseEntity<PublicSurveySectionDto> getSingleSurveySection(
            @PathVariable Long companyId,
            @PathVariable String language,
            @PathVariable Long sectionId) {
        PublicSurveySectionDto surveySection = publicApiService.getSingleSurveySection(companyId, language, sectionId);
        return ResponseEntity.ok(surveySection);
    }
}
