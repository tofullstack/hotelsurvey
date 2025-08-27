package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.ReportSummaryDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.service.ReportService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/responses")
    public ResponseEntity<Map<String, Object>> getSurveyResponses(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String serieEmpresa,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<SurveyResponseDetailDto> responsesPage = reportService.getFilteredSurveyResponses(
                companyId,
                companyName,
                serieEmpresa,
                language,
                startDate,
                endDate,
                pageable
        );

        // Cria um Map com os dados paginados para garantir a estabilidade da resposta
        Map<String, Object> response = new HashMap<>();
        response.put("content", responsesPage.getContent());
        response.put("totalPages", responsesPage.getTotalPages());
        response.put("totalElements", responsesPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para download de relatórios.
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam String format,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String serieEmpresa,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws IOException, DocumentException {

        byte[] data = reportService.downloadReport(
                companyId,
                companyName,
                serieEmpresa,
                language,
                startDate,
                endDate,
                format
        );

        String filename = "relatorio." + format;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(format)) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("xml".equalsIgnoreCase(format)) {
            mediaType = MediaType.APPLICATION_XML;
        } else {
            return ResponseEntity.badRequest().body("Formato inválido".getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(data);
    }

    @GetMapping("/responses/{serieEmpresa}")
    public ResponseEntity<List<SurveyResponseDetailDto>> getSurveyResponsesBySerie(@PathVariable String serieEmpresa) {
        List<SurveyResponseDetailDto> responses = reportService.getSurveyResponsesBySerie(serieEmpresa);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportSummaryDto> getSummary(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String serieEmpresa,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ReportSummaryDto summary = reportService.getReportSummary(
                companyId,
                companyName,
                serieEmpresa,
                language,
                startDate,
                endDate
        );
        return ResponseEntity.ok(summary);
    }
}