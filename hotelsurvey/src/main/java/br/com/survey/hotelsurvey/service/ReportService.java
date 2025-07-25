package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionAnswerDetailDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.entity.QuestionAnswer;
import br.com.survey.hotelsurvey.entity.SurveyResponse;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.repository.SurveyResponseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Import for StringUtils

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    /**
     * Busca todas as respostas de formulários cadastradas.
     * Pode ser estendido com filtros por empresa, idioma, data, etc.
     * @return Lista de SurveyResponseDetailDto.
     */
    public List<SurveyResponseDetailDto> getAllSurveyResponses() {
        return surveyResponseRepository.findAll().stream()
                .map(this::convertToSurveyResponseDetailDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma resposta de formulário específica pelo seu ID.
     * @param id ID da resposta do formulário.
     * @return SurveyResponseDetailDto.
     * @throws ResourceNotFoundException Se a resposta não for encontrada.
     */
    public SurveyResponseDetailDto getSurveyResponseById(Long id) {
        return surveyResponseRepository.findById(id)
                .map(this::convertToSurveyResponseDetailDto)
                .orElseThrow(() -> new ResourceNotFoundException("Survey response not found with ID: " + id));
    }

    /**
     * Busca respostas de formulários filtradas por empresa e/ou idioma e/ou período.
     * Este é um exemplo básico; pode ser expandido com Specification ou QueryDsl para filtros mais complexos.
     *
     * @param companyId   (Opcional) Filtra por ID da empresa.
     * @param companyName (Opcional) Filtra pelo nome da empresa (parcial e case-insensitive).
     * @param language    (Opcional) Filtra por idioma.
     * @param startDate   (Opcional) Filtra respostas a partir desta data/hora.
     * @param endDate     (Opcional) Filtra respostas até esta data/hora.
     * @return Lista de SurveyResponseDetailDto.
     */
    public List<SurveyResponseDetailDto> getFilteredSurveyResponses(
            Long companyId,
            String companyName, // New parameter
            String language,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // implementação simples de filtragem em memória.
        // para grandes volumes de dados, considere usar métodos de repositório com @Query
        // ou Spring Data JPA Specification/QueryDsl para filtragem no banco de dados.
        List<SurveyResponse> responses = surveyResponseRepository.findAll();

        return responses.stream()
                .filter(response -> companyId == null || response.getCompany().getId().equals(companyId))
                .filter(response -> !StringUtils.hasText(companyName) ||
                        (response.getCompany() != null &&
                                StringUtils.hasText(response.getCompany().getName()) &&
                                response.getCompany().getName().toLowerCase().contains(companyName.toLowerCase())))
               // .filter(response -> language == null || response.getLanguage().equalsIgnoreCase(language))
                .filter(response -> startDate == null || response.getResponseDate().isAfter(startDate) || response.getResponseDate().isEqual(startDate))
                .filter(response -> endDate == null || response.getResponseDate().isBefore(endDate) || response.getResponseDate().isEqual(endDate))
                .map(this::convertToSurveyResponseDetailDto)
                .collect(Collectors.toList());
    }


    // Helper para converter entidade SurveyResponse para SurveyResponseDetailDto
    private SurveyResponseDetailDto convertToSurveyResponseDetailDto(SurveyResponse entity) {
        SurveyResponseDetailDto dto = new SurveyResponseDetailDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany().getId());
        dto.setCompanyName(entity.getCompany().getName()); // Assume que Company tem um campo 'name'
        //dto.setLanguage(entity.getLanguage());
        dto.setResponseDate(entity.getResponseDate());
        dto.setGuestIdentifier(entity.getGuestIdentifier());
        dto.setFreeTextFeedback(entity.getFreeTextFeedback());
        dto.setAnswers(entity.getAnswers().stream()
                .map(this::convertToQuestionAnswerDetailDto)
                .collect(Collectors.toList()));
        return dto;
    }

    // Helper para converter entidade QuestionAnswer para QuestionAnswerDetailDto
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
}