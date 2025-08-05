package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.ConditionalTriggerRequest;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.ConditionalSectionTrigger;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.mapper.SurveySectionMapper;
import br.com.survey.hotelsurvey.repository.ConditionalSectionTriggerRepository;
import br.com.survey.hotelsurvey.repository.QuestionRepository;
import br.com.survey.hotelsurvey.repository.QuestionTranslationRepository;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConditionalSectionTriggerService {

    private final ConditionalSectionTriggerRepository triggerRepository;
    private final SurveySectionRepository sectionRepository;
    private final QuestionRepository questionRepository;
    private final SurveySectionMapper sectionMapper;
    private final QuestionTranslationRepository questionTranslationRepository;

    public ConditionalSectionTrigger createTrigger(ConditionalTriggerRequest request) {
        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Pergunta não encontrada"));

        SurveySection section = sectionRepository.findById(request.getTargetSectionId())
                .orElseThrow(() -> new RuntimeException("Seção alvo não encontrada"));


        ConditionalSectionTrigger trigger = new ConditionalSectionTrigger();
        trigger.setQuestion(question);
        trigger.setTargetSection(section);
        trigger.setTriggerValue(request.getTriggerValue());

        return triggerRepository.save(trigger);
    }


    public List<SurveySectionDto> getTriggeredSectionsWithTranslations(Long questionId, String answerValue, String language) {
        List<ConditionalSectionTrigger> triggers = triggerRepository.findByQuestionId(questionId);

        return triggers.stream()
                .filter(trigger -> {
                    // Converte a string "1,2" para uma lista de strings
                    List<String> triggerValues = Arrays.asList(trigger.getTriggerValue().split(","));
                    return triggerValues.contains(answerValue);
                })
                .map(ConditionalSectionTrigger::getTargetSection)
                .map(section ->
                        sectionMapper.toDtoWithTranslatedQuestions(section, language, questionTranslationRepository)
                )
                .toList();
    }
}