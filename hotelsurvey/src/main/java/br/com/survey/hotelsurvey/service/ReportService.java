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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.awt.Color;
import java.awt.Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

@Service
public class ReportService {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    private static final BaseColor PRIMARY_BLUE = new BaseColor(40, 116, 166);
    private static final BaseColor LIGHT_BLUE = new BaseColor(220, 236, 245);
    private static final BaseColor LIGHT_BLUE_BORDER = new BaseColor(190, 222, 239);
    private static final BaseColor DARK_TEXT = new BaseColor(50, 50, 50);
    private static final BaseColor LIGHT_GREY = new BaseColor(240, 240, 240);
    private static final BaseColor WHITE = BaseColor.WHITE;
    private static final BaseColor BLACK = BaseColor.BLACK;

    private static final java.awt.Color JFREE_PRIMARY_BLUE = new java.awt.Color(40, 116, 166);
    private static final java.awt.Color JFREE_DARK_TEXT = new java.awt.Color(50, 50, 50);

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

        ReportSummaryDto summary = getReportSummary(companyId, companyName, serieEmpresa, language, startDate, endDate);

        if ("pdf".equalsIgnoreCase(format)) {
            return generatePdfReport(responses, summary);
        } else if ("xml".equalsIgnoreCase(format)) {
            return generateXmlReport(responses);
        } else {
            throw new IllegalArgumentException("Formato de relatório inválido: " + format);
        }
    }

    private byte[] generatePdfReport(List<SurveyResponseDetailDto> responses, ReportSummaryDto summary) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            com.itextpdf.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE);
            Paragraph mainTitle = new Paragraph("Relatório de Pesquisas", fontTitle);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);
            document.add(Chunk.NEWLINE);

            addSummarySection(document, summary);
            document.add(Chunk.NEWLINE);

            addChartSection(document, responses);
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("Detalhes de Respostas Individuais", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_BLUE)));
            document.add(Chunk.NEWLINE);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (SurveyResponseDetailDto response : responses) {
                document.add(createResponseDetailTable(response, formatter));
                document.add(Chunk.NEWLINE);
                document.add(new Phrase("----------------------------------------------------------------------------------------------------------------------------------"));
                document.add(Chunk.NEWLINE);
                document.add(Chunk.NEWLINE);
            }

            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
            throw new DocumentException("Erro ao gerar PDF.");
        }

        return baos.toByteArray();
    }

    private void addSummarySection(Document document, ReportSummaryDto summary) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{1, 2});
        summaryTable.setSpacingBefore(10f);
        summaryTable.setSpacingAfter(10f);

        PdfPCell titleCell = new PdfPCell(new Phrase("Sumário do Relatório", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE)));

        titleCell.setBackgroundColor(PRIMARY_BLUE);
        titleCell.setColspan(2);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setPadding(8);
        summaryTable.addCell(titleCell);

        com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);

        addCellWithLabelAndData(summaryTable, "Total de Respostas:", String.valueOf(summary.getTotalResponses()), dataFont);
        if (summary.getAverageRating() != null) {
            addCellWithLabelAndData(summaryTable, "Média de Avaliação:", String.format("%.2f", summary.getAverageRating()), dataFont);
        }
        if (summary.getRatingStandardDeviation() != null) {
            addCellWithLabelAndData(summaryTable, "Desvio Padrão:", String.format("%.2f", summary.getRatingStandardDeviation()), dataFont);
        }

        document.add(summaryTable);
    }

    private void addCellWithLabelAndData(PdfPTable table, String label, String data, com.itextpdf.text.Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setBackgroundColor(LIGHT_GREY);
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell dataCell = new PdfPCell(new Phrase(data, font));
        dataCell.setBorder(Rectangle.NO_BORDER);
        dataCell.setPadding(5);
        table.addCell(dataCell);
    }

    private void addChartSection(Document document, List<SurveyResponseDetailDto> responses) throws DocumentException, IOException {
        com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, PRIMARY_BLUE);
        document.add(new Paragraph("Gráficos de Avaliação", subtitleFont));
        document.add(Chunk.NEWLINE);

        byte[] chartBytes = generateRatingDistributionChart(responses);
        Image chartImage = Image.getInstance(chartBytes);
        chartImage.setAlignment(Element.ALIGN_CENTER);
        chartImage.scaleToFit(350, 200);
        document.add(chartImage);
    }

    private PdfPTable createResponseDetailTable(SurveyResponseDetailDto response, DateTimeFormatter formatter) throws DocumentException {
        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);
        mainTable.setSpacingBefore(10f);
        mainTable.setSpacingAfter(10f);
        mainTable.getDefaultCell().setPadding(5);
        mainTable.getDefaultCell().setBorderColor(LIGHT_BLUE_BORDER);

        PdfPCell headerCell = new PdfPCell(new Phrase("Detalhes da Resposta", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE)));
        headerCell.setBackgroundColor(LIGHT_BLUE);
        headerCell.setBorderColor(LIGHT_BLUE_BORDER);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setPadding(8);
        mainTable.addCell(headerCell);

        PdfPTable dataTable = new PdfPTable(2);
        dataTable.setWidthPercentage(100);
        dataTable.setWidths(new float[]{1.5f, 3.5f});
        dataTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        dataTable.getDefaultCell().setPadding(5);

        com.itextpdf.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);
        com.itextpdf.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);

        dataTable.addCell(new Phrase("ID da Resposta:", labelFont));
        dataTable.addCell(new Phrase(String.valueOf(response.getId()), valueFont));

        dataTable.addCell(new Phrase("Data da Resposta:", labelFont));
        dataTable.addCell(new Phrase(response.getResponseDate().format(formatter), valueFont));

        dataTable.addCell(new Phrase("Empresa:", labelFont));
        dataTable.addCell(new Phrase(response.getCompanyName(), valueFont));

        dataTable.addCell(new Phrase("Série da Empresa:", labelFont));
        dataTable.addCell(new Phrase(response.getSerieEmpresa(), valueFont));

        dataTable.addCell(new Phrase("Identificador do Cliente:", labelFont));
        dataTable.addCell(new Phrase(response.getGuestIdentifier() != null ? response.getGuestIdentifier() : "N/A", valueFont));

        dataTable.addCell(new Phrase("Idioma da Resposta:", labelFont));
        dataTable.addCell(new Phrase(response.getLanguage() != null ? response.getLanguage() : "N/A", valueFont));

        PdfPCell dataCell = new PdfPCell(dataTable);
        dataCell.setBorder(Rectangle.NO_BORDER);
        dataCell.setPadding(0);
        mainTable.addCell(dataCell);

        if (StringUtils.hasText(response.getFreeTextFeedback())) {
            PdfPCell feedbackHeader = new PdfPCell(new Phrase("Detalhes do Feedback do Cliente:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT)));
            feedbackHeader.setBorder(Rectangle.NO_BORDER);
            feedbackHeader.setPadding(5);
            mainTable.addCell(feedbackHeader);

            PdfPCell feedbackCell = new PdfPCell(new Phrase(response.getFreeTextFeedback(), valueFont));
            feedbackCell.setBorderColor(LIGHT_BLUE_BORDER);
            feedbackCell.setBackgroundColor(LIGHT_BLUE);
            feedbackCell.setPadding(8);
            mainTable.addCell(feedbackCell);
        }

        return mainTable;
    }

    private byte[] generateXmlReport(List<SurveyResponseDetailDto> responses) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlMapper.writeValue(baos, responses);

        return baos.toByteArray();
    }

    private byte[] generateRatingDistributionChart(List<SurveyResponseDetailDto> responses) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Long> ratingCounts = responses.stream()
                .flatMap(r -> r.getAnswers().stream())
                .filter(a -> a.getQuestionType() == QuestionType.SCALE && a.getAnswerValue() != null)
                .collect(Collectors.groupingBy(QuestionAnswerDetailDto::getAnswerValue, Collectors.counting()));

        for (int i = 1; i <= 5; i++) {
            dataset.addValue(ratingCounts.getOrDefault(String.valueOf(i), 0L), "Avaliações", String.valueOf(i));
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Distribuição de Avaliações",
                "Nota",
                "Número de Respostas",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        barChart.setBackgroundPaint(java.awt.Color.WHITE);
        barChart.setBorderVisible(false);

        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(new java.awt.Color(230, 230, 230));
        plot.setRangeGridlinePaint(new java.awt.Color(230, 230, 230));
        plot.setOutlineVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(org.jfree.chart.axis.CategoryLabelPositions.STANDARD);
        domainAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 12));
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setSeriesPaint(0, JFREE_PRIMARY_BLUE);
        renderer.setMaximumBarWidth(0.10);

        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 10));
        renderer.setDefaultItemLabelPaint(JFREE_DARK_TEXT);
        ItemLabelPosition p = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER
        );
        renderer.setDefaultPositiveItemLabelPosition(p);

        barChart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 18));
        barChart.getTitle().setPaint(JFREE_DARK_TEXT);

        barChart.getLegend().setItemFont(new Font("SansSerif", Font.PLAIN, 10));
        barChart.getLegend().setFrame(org.jfree.chart.block.BlockBorder.NONE);
        barChart.getLegend().setPadding(new RectangleInsets(5, 10, 5, 10));

        ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartBaos, barChart, 600, 400);
        return chartBaos.toByteArray();
    }

    private SurveyResponseDetailDto convertToSurveyResponseDetailDto(SurveyResponse entity) {
        SurveyResponseDetailDto dto = new SurveyResponseDetailDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany().getId());
        dto.setCompanyName(entity.getCompany().getName());
        dto.setSerieEmpresa(entity.getCompany().getSerieEmpresa());
        dto.setLanguage(entity.getLanguage());
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

        List<Double> ratings = new ArrayList<>();
        for (SurveyResponseDetailDto responseDto : detailDtos) {
            for (QuestionAnswerDetailDto answerDto : responseDto.getAnswers()) {
                if (answerDto.getQuestionType() == QuestionType.SCALE && !Boolean.TRUE.equals(answerDto.getDidNotUseService())) {
                    try {
                        ratings.add(Double.parseDouble(answerDto.getAnswerValue()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        double totalRating = ratings.stream().mapToDouble(Double::doubleValue).sum();
        long ratingCount = ratings.size();
        Double averageRating = (ratingCount > 0) ? totalRating / ratingCount : null;

        Double standardDeviation = null;
        if (ratingCount > 1) {
            double sumOfSquares = 0;
            for (Double rating : ratings) {
                sumOfSquares += Math.pow(rating - averageRating, 2);
            }
            standardDeviation = Math.sqrt(sumOfSquares / (ratingCount - 1));
        }

        return new ReportSummaryDto(responses.size(), averageRating, standardDeviation);
    }


    // implementacao de busca por pesquisa unica
    public Optional<SurveyResponseDetailDto> getSurveyResponseById(Long id) {
        return surveyResponseRepository.findById(id)
                .map(this::convertToSurveyResponseDetailDto);
    }

    public byte[] generatePdfForSingleResponse(SurveyResponseDetailDto response) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4, 30, 30, 30, 30);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            com.itextpdf.text.Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_BLUE);
            Paragraph mainTitle = new Paragraph("Relatório de Avaliação Individual", fontTitle);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(mainTitle);
            document.add(Chunk.NEWLINE);

            com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6, DARK_TEXT);
            document.add(new Paragraph("Detalhes da Resposta", subtitleFont));
            document.add(Chunk.NEWLINE);

            PdfPTable responseDetails = new PdfPTable(2);
            responseDetails.setWidthPercentage(100);
            responseDetails.setWidths(new float[]{1.5f, 3.5f});
            responseDetails.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            responseDetails.getDefaultCell().setPadding(5);

            com.itextpdf.text.Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT);
            com.itextpdf.text.Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, DARK_TEXT);

            responseDetails.addCell(new Phrase("ID da Resposta:", labelFont));
            responseDetails.addCell(new Phrase(String.valueOf(response.getId()), valueFont));
            responseDetails.addCell(new Phrase("Identificador do Cliente:", labelFont));
            responseDetails.addCell(new Phrase(String.valueOf(response.getGuestIdentifier()), valueFont));
            responseDetails.addCell(new Phrase("Empresa:", labelFont));
            responseDetails.addCell(new Phrase(response.getCompanyName(), valueFont));
            responseDetails.addCell(new Phrase("Série:", labelFont));
            responseDetails.addCell(new Phrase(response.getSerieEmpresa(), valueFont));
            responseDetails.addCell(new Phrase("Idioma:", labelFont));
            responseDetails.addCell(new Phrase(response.getLanguage(), valueFont));
            responseDetails.addCell(new Phrase("Data da Resposta:", labelFont));
            responseDetails.addCell(new Phrase(response.getResponseDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), valueFont));

            document.add(responseDetails);
            document.add(Chunk.NEWLINE);

            double totalRating = 0;
            long ratingCount = 0;
            List<QuestionAnswerDetailDto> scaleAnswers = response.getAnswers().stream()
                    .filter(a -> a.getQuestionType() == QuestionType.SCALE && !Boolean.TRUE.equals(a.getDidNotUseService()))
                    .collect(Collectors.toList());

            for (QuestionAnswerDetailDto answer : scaleAnswers) {
                try {
                    totalRating += Double.parseDouble(answer.getAnswerValue());
                    ratingCount++;
                } catch (NumberFormatException ignored) {}
            }
            Double averageRating = (ratingCount > 0) ? totalRating / ratingCount : null;

            if (averageRating != null) {
                document.add(new Paragraph(String.format("Média de Avaliação (Escala): %.2f", averageRating), subtitleFont));
                document.add(Chunk.NEWLINE);
            }

            if (!scaleAnswers.isEmpty()) {
                DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                Map<String, Long> ratingCounts = scaleAnswers.stream()
                        .collect(Collectors.groupingBy(QuestionAnswerDetailDto::getAnswerValue, Collectors.counting()));
                for (int i = 1; i <= 5; i++) {
                    dataset.addValue(ratingCounts.getOrDefault(String.valueOf(i), 0L), "Avaliações", String.valueOf(i));
                }
                JFreeChart barChart = createJFreeBarChart(dataset, "Distribuição das Avaliações");
                ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
                ChartUtils.writeChartAsPNG(chartBaos, barChart, 300, 250);
                Image chartImage = Image.getInstance(chartBaos.toByteArray());
                chartImage.setAlignment(Element.ALIGN_CENTER);
                document.add(chartImage);
                document.add(Chunk.NEWLINE);
            }

            document.add(new Paragraph("Perguntas e Respostas Detalhadas", subtitleFont));
            document.add(Chunk.NEWLINE);

            for (QuestionAnswerDetailDto answer : response.getAnswers()) {
                PdfPTable qaTable = new PdfPTable(2);
                qaTable.setWidthPercentage(100);
                qaTable.setWidths(new float[]{1.5f, 3.5f});
                qaTable.setSpacingAfter(10f);

                PdfPCell questionCell = new PdfPCell(new Phrase(answer.getQuestionLabel(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, DARK_TEXT)));
                questionCell.setBorder(Rectangle.NO_BORDER);
                questionCell.setPadding(5);
                qaTable.addCell(questionCell);

                String answerValue = answer.getDidNotUseService() ? "Não utilizou o serviço" : answer.getAnswerValue();
                PdfPCell answerCell = new PdfPCell(new Phrase(answerValue != null ? answerValue : "N/A", valueFont));
                answerCell.setBorder(Rectangle.NO_BORDER);
                answerCell.setBackgroundColor(LIGHT_GREY);
                answerCell.setPadding(5);
                qaTable.addCell(answerCell);

                document.add(qaTable);
            }

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            e.printStackTrace();
            throw new DocumentException("Erro ao gerar PDF para resposta única.");
        }
    }

    // método auxiliar para criar o JFreeChart
    private JFreeChart createJFreeBarChart(DefaultCategoryDataset dataset, String title) {
        JFreeChart barChart = ChartFactory.createBarChart(
                title,
                "Nota",
                "Número de Respostas",
                dataset,
                PlotOrientation.VERTICAL,
                false, // Remover legenda
                true,
                false
        );
        barChart.setBackgroundPaint(java.awt.Color.WHITE);
        barChart.setBorderVisible(false);
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setBackgroundPaint(java.awt.Color.WHITE);
        plot.setDomainGridlinePaint(new java.awt.Color(230, 230, 230));
        plot.setRangeGridlinePaint(new java.awt.Color(230, 230, 230));
        plot.setOutlineVisible(false);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, JFREE_PRIMARY_BLUE);
        renderer.setMaximumBarWidth(0.15);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new java.awt.Font("SansSerif", Font.BOLD, 10));
        renderer.setDefaultItemLabelPaint(JFREE_DARK_TEXT);
        ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
        renderer.setDefaultPositiveItemLabelPosition(p);
        barChart.getTitle().setFont(new java.awt.Font("SansSerif", Font.BOLD, 14));
        barChart.getTitle().setPaint(JFREE_DARK_TEXT);
        return barChart;
    }
}