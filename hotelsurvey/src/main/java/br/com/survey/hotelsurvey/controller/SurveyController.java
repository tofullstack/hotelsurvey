package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.model.Formulario;
import br.com.survey.hotelsurvey.service.SurveyPublicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/survey")
public class SurveyController {

    @Autowired
    private SurveyPublicoService surveyPublicoService;

    // Protegido pela regra geral `anyRequest().authenticated()` do SecurityConfig
    @GetMapping("/questions")
    public ResponseEntity<List<Formulario>> getSurveyQuestions(
            @RequestParam String idioma,
            @RequestParam Long idEmpresa) {

        List<Formulario> formularios = surveyPublicoService.buscarFormulariosPublicos(idEmpresa, idioma);
        return ResponseEntity.ok(formularios);
    }
}
