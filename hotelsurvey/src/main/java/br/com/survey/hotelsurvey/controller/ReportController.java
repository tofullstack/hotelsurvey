package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.ReportSummaryDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.service.ReportService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'USUARIO')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/responses")
    public ResponseEntity<ReportSummaryDto> getSurveyResponses( // <-- Alterado o tipo de retorno
                                                                @RequestParam(required = false) Long companyId,
                                                                @RequestParam(required = false) String companyName,
                                                                @RequestParam(required = false) String serieEmpresa,
                                                                @RequestParam(required = false) String language,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        ReportSummaryDto summary = reportService.getFilteredSurveyResponses(
                companyId,
                companyName,
                serieEmpresa,
                language,
                startDate,
                endDate
        );
        return ResponseEntity.ok(summary);
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
}