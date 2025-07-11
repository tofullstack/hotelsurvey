package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.PublicApiDTO.IFormularioDTO;
import br.com.survey.hotelsurvey.service.PublicApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class PublicSurveyController {

    private final PublicApiService publicApiService;

    public PublicSurveyController(PublicApiService publicApiService) {
        this.publicApiService = publicApiService;
    }

    @GetMapping("/api/survey/questions")
    public ResponseEntity<List<IFormularioDTO>> getSurveyQuestions(
            @RequestParam("idioma") String language,
            @RequestParam("idEmpresa") Long companyId) {

        List<IFormularioDTO> surveyStructure = publicApiService.getSurveyQuestions(companyId, language);
        return ResponseEntity.ok(surveyStructure);
    }
}