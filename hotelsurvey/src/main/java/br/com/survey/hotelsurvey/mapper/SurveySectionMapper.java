
package br.com.survey.hotelsurvey.mapper;

import br.com.survey.hotelsurvey.dto.QuestionAnswerDetailDto;
import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.QuestionAnswer;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.repository.QuestionTranslationRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveySectionMapper {

    public static SurveySectionDto toDto(SurveySection section) {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(section.getId());
        dto.setName(section.getName());
        dto.setActive(section.getActive());
        dto.setQuestions(
                section.getQuestions().stream()
                        .map(SurveySectionMapper::toQuestionDto)
                        .collect(Collectors.toList())
        );

        if (section.getCompany() != null) {
            dto.setCompanyId(section.getCompany().getId());
            dto.setCompanyName(section.getCompany().getName());

            dto.setSerieEmpresa(section.getCompany().getSerieEmpresa());
            dto.setLanguage(section.getLanguage());

        }


        return dto;
    }


    private static QuestionDto toQuestionDto(Question q) {
        return new QuestionDto(
                q.getId(),
                q.getSurveySection() != null ? q.getSurveySection().getId() : null,
                q.getType(),
                q.getLabel(),
                q.getDeniable() != null ? q.getDeniable() : false,
                q.getMandatory() != null ? q.getMandatory() : false,
                q.getOptions()
        );
    }




    //método que surveySection para DTO com as questões traduzidas inclusas
    public SurveySectionDto toDtoWithTranslatedQuestions(SurveySection section, String language, QuestionTranslationRepository translationRepository) {
        SurveySectionDto dto = toDto(section);
        if (section.getQuestions() != null) {
            List<QuestionDto> translatedQuestions = section.getQuestions().stream().map(question -> {
                QuestionDto questionDto = new QuestionDto();
                questionDto.setId(question.getId());
                questionDto.setType(question.getType());
                questionDto.setMandatory(question.getMandatory());
                questionDto.setDeniable(question.getDeniable());
                questionDto.setOptions(question.getOptions());

                // aqui é onde busca a tradução
                translationRepository.findByQuestionIdAndLanguage(question.getId(), language)
                        .ifPresent(translation -> questionDto.setLabel(translation.getLabel()));

                return questionDto;
            }).toList();
            dto.setQuestions(translatedQuestions);
        }
        return dto;
    }


}

