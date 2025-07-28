
package br.com.survey.hotelsurvey.mapper;

import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveySectionMapper {

    public static SurveySectionDto toDto(SurveySection section) {
        List<QuestionDto> questions = section.getQuestions().stream()
                .map(SurveySectionMapper::toQuestionDto)
                .collect(Collectors.toList());


        return new SurveySectionDto(
                section.getId(),             // 1º
                section.getName(),           // 2º
                section.getCompany().getId(),// 3º
                section.getActive(),         // 4º
                questions                    // 5º
        );
    }

    private static QuestionDto toQuestionDto(Question q) {
        return new QuestionDto(
                q.getId(),
                q.getType(),       // QuestionType
                q.getLabel(),      // String
                // Lida com Boolean para boolean primitivo
                q.getDeniable() != null ? q.getDeniable() : false,
                q.getMandatory() != null ? q.getMandatory() : false,
                // AQUI ESTÁ O PROBLEMA DA LINHA 38
                q.getRequired() != null ? q.getRequired() : false,
                q.getOptions()     // List<String>
        );
    }
}

