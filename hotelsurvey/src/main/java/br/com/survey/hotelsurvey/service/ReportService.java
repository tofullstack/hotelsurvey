package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionAnswerDetailDto;
import br.com.survey.hotelsurvey.dto.ReportSummaryDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.entity.QuestionAnswer;
import br.com.survey.hotelsurvey.entity.QuestionType;
import br.com.survey.hotelsurvey.entity.SurveyResponse;
import br.com.survey.hotelsurvey.repository.SurveyResponseRepository;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ReportService {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    public List<SurveyResponseDetailDto> getSurveyResponsesBySerie(String serieEmpresa) {
        return surveyResponseRepository.findByCompanySerieEmpresaIgnoreCase(serieEmpresa).stream()
                .map(this::convertToSurveyResponseDetailDto)
                .collect(Collectors.toList());
    }

    public Page<SurveyResponseDetailDto> getFilteredSurveyResponses(
            Long companyId,
            String companyName,
            String serieEmpresa,
            String language,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        Specification<SurveyResponse> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }
            if (StringUtils.hasText(companyName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("company").get("name")), "%" + companyName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(serieEmpresa)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("company").get("serieEmpresa")), serieEmpresa.toLowerCase()));
            }
            if (StringUtils.hasText(language)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("language")), language.toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("responseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("responseDate"), endDate.plusSeconds(1)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<SurveyResponse> responsesPage = surveyResponseRepository.findAll(spec, pageable);

        return responsesPage.map(this::convertToSurveyResponseDetailDto);
    }

    public byte[] downloadReport(
            Long companyId,
            String companyName,
            String serieEmpresa,
            String language,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String format) throws IOException, DocumentException {

        Pageable pageableAll = Pageable.unpaged();

        List<SurveyResponseDetailDto> responses = getFilteredSurveyResponses(
                companyId,
                companyName,
                serieEmpresa,
                language,
                startDate,
                endDate,
                pageableAll
        ).getContent();

        if ("pdf".equalsIgnoreCase(format)) {
            return generatePdfReport(responses);
        } else if ("xml".equalsIgnoreCase(format)) {
            return generateXmlReport(responses);
        } else {
            throw new IllegalArgumentException("Formato de relatório inválido: " + format);
        }
    }

    private byte[] generatePdfReport(List<SurveyResponseDetailDto> responses) throws DocumentException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Relatório de Pesquisas", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new PdfPCell(new Phrase("ID", fontHeader)));
            table.addCell(new PdfPCell(new Phrase("Empresa", fontHeader)));
            table.addCell(new PdfPCell(new Phrase("Data", fontHeader)));
            table.addCell(new PdfPCell(new Phrase("Feedback", fontHeader)));

            Font fontData = FontFactory.getFont(FontFactory.HELVETICA, 10);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (SurveyResponseDetailDto response : responses) {
                table.addCell(new PdfPCell(new Phrase(response.getId().toString(), fontData)));
                table.addCell(new PdfPCell(new Phrase(response.getCompanyName(), fontData)));
                table.addCell(new PdfPCell(new Phrase(response.getResponseDate().format(formatter), fontData)));
                table.addCell(new PdfPCell(new Phrase(response.getFreeTextFeedback(), fontData)));
            }

            document.add(table);

            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new DocumentException("Erro ao gerar PDF.");
        }

        return baos.toByteArray();
    }

    private byte[] generateXmlReport(List<SurveyResponseDetailDto> responses) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlMapper.writeValue(baos, responses);

        return baos.toByteArray();
    }

    private SurveyResponseDetailDto convertToSurveyResponseDetailDto(SurveyResponse entity) {
        SurveyResponseDetailDto dto = new SurveyResponseDetailDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany().getId());
        dto.setCompanyName(entity.getCompany().getName());
        dto.setSerieEmpresa(entity.getCompany().getSerieEmpresa());
        dto.setResponseDate(entity.getResponseDate());
        dto.setGuestIdentifier(entity.getGuestIdentifier());
        dto.setFreeTextFeedback(entity.getFreeTextFeedback());
        dto.setAnswers(entity.getAnswers().stream()
                .map(this::convertToQuestionAnswerDetailDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private QuestionAnswerDetailDto convertToQuestionAnswerDetailDto(QuestionAnswer entity) {
        QuestionAnswerDetailDto dto = new QuestionAnswerDetailDto();
        dto.setAnswerId(entity.getId());
        dto.setQuestionId(entity.getQuestion().getId());
        dto.setQuestionLabel(entity.getQuestion().getLabel());
        dto.setQuestionType(entity.getQuestion().getType());
        dto.setSurveySectionId(entity.getSurveySection().getId());
        dto.setSurveySectionName(entity.getSurveySection().getName());
        dto.setAnswerValue(entity.getAnswerValue());
        dto.setDidNotUseService(entity.getDidNotUseService());
        return dto;
    }


    public ReportSummaryDto getReportSummary(
            Long companyId,
            String companyName,
            String serieEmpresa,
            String language,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        Specification<SurveyResponse> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (companyId != null) {
                predicates.add(criteriaBuilder.equal(root.get("company").get("id"), companyId));
            }
            if (StringUtils.hasText(companyName)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("company").get("name")), "%" + companyName.toLowerCase() + "%"));
            }
            if (StringUtils.hasText(serieEmpresa)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("company").get("serieEmpresa")), serieEmpresa.toLowerCase()));
            }
            if (StringUtils.hasText(language)) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("language")), language.toLowerCase()));
            }
            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("responseDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("responseDate"), endDate.plusSeconds(1)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        List<SurveyResponse> responses = surveyResponseRepository.findAll(spec);

        List<SurveyResponseDetailDto> detailDtos = responses.stream()
                .map(this::convertToSurveyResponseDetailDto)
                .collect(Collectors.toList());

        double totalRating = 0;
        long ratingCount = 0;

        for (SurveyResponseDetailDto responseDto : detailDtos) {
            for (QuestionAnswerDetailDto answerDto : responseDto.getAnswers()) {
                if (answerDto.getQuestionType() == QuestionType.SCALE && !Boolean.TRUE.equals(answerDto.getDidNotUseService())) {
                    try {
                        totalRating += Double.parseDouble(answerDto.getAnswerValue());
                        ratingCount++;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        Double averageRating = (ratingCount > 0) ? totalRating / ratingCount : null;

        return new ReportSummaryDto(responses.size(), averageRating, detailDtos);
    }
}