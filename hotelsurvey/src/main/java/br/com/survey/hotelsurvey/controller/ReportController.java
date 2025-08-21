package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Retorna todas as respostas de formulários ou respostas filtradas.
     * Exemplo de uso:
     * GET /api/reports/responses
     * GET /api/reports/responses?companyId=1&language=pt-BR
     * GET /api/reports/responses?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59
     * testandono insomnia <-
     * @param companyId (Opcional) Filtra por ID da empresa.
     * @param companyName (Opcional) Filtra pelo nome da empresa.
     * @param language (Opcional) Filtra por idioma.
     * @param startDate (Opcional) Data de início para o filtro (formato ISO: YYYY-MM-DDTHH:mm:ss).
     * @param endDate (Opcional) Data de fim para o filtro (formato ISO: YYYY-MM-DDTHH:mm:ss).
     * @return Lista de SurveyResponseDetailDto.
     */
    @GetMapping("/responses")
    public ResponseEntity<List<SurveyResponseDetailDto>> getSurveyResponses(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String serieEmpresa, // novo parâmetro
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<SurveyResponseDetailDto> responses = reportService.getFilteredSurveyResponses(
                companyId,
                companyName,
                serieEmpresa,  // novo campo
                language,
                startDate,
                endDate
        );
        return ResponseEntity.ok(responses);
    }

    /**
     * retorna os detalhes de uma resposta de formulário específica.
     * GET /api/reports/responses/{serie}
     *
     * @param serieEmpresa ID da resposta do formulário.
     * @return SurveyResponseDetailDto.
     */
    @GetMapping("/responses/{serieEmpresa}")
    public ResponseEntity<SurveyResponseDetailDto> getSurveyResponseBySerie(@PathVariable String serieEmpresa) {
        SurveyResponseDetailDto response = reportService.getSurveyResponseBySerie(serieEmpresa);
        return ResponseEntity.ok(response);
    }


}