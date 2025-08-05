package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.PublicQuestionDto;
import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.QuestionTranslation;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PublicApiService {

    @Autowired
    private SurveySectionRepository surveySectionRepository;

    public PublicSurveySectionDto getSingleSurveySectionByFormIdAndLanguage(Long formId, String language) {
        Optional<SurveySection> sectionOptional = surveySectionRepository.findWithQuestionsById(formId);

        SurveySection section = sectionOptional
                .orElseThrow(() -> new ResourceNotFoundException("Survey section with ID " + formId + " not found."));

        // Se o formulário não estiver ativo, lança uma exceção.
        if (!section.getActive()) {
            throw new ResourceNotFoundException("Survey section with ID " + formId + " not active.");
        }

        return convertToPublicSurveySectionDto(section, language);
    }

    private PublicSurveySectionDto convertToPublicSurveySectionDto(SurveySection entity, String language) {
        PublicSurveySectionDto dto = new PublicSurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCompanyId(entity.getCompany().getId()); // <-- ADICIONE ESTA LINHA PARA PREENCHER O CAMPO
        dto.setQuestions(entity.getQuestions().stream()
                .map(question -> toPublicDto(question, language))
                .collect(Collectors.toList())
        );

        return dto;
    }

    public PublicQuestionDto toPublicDto(Question question, String language) {
        PublicQuestionDto dto = new PublicQuestionDto();
        dto.setId(question.getId());
        dto.setType(question.getType().name());
        dto.setMandatory(question.getMandatory());
        dto.setRequired(question.getRequired());
        dto.setOptions(question.getOptions());

        String label = question.getTranslations().stream()
                .filter(t -> t.getLanguage().equalsIgnoreCase(language))
                .map(QuestionTranslation::getLabel)
                .findFirst()
                .orElse("Tradução indisponível");

        dto.setLabel(label);
        return dto;
    }
}