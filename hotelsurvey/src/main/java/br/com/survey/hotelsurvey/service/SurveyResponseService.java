package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionAnswerDetailDto;
import br.com.survey.hotelsurvey.dto.QuestionAnswerRequest;
import br.com.survey.hotelsurvey.dto.SurveyResponseDetailDto;
import br.com.survey.hotelsurvey.dto.SurveyResponseRequest;
import br.com.survey.hotelsurvey.entity.*;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class SurveyResponseService {

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private SurveySectionRepository surveySectionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    /**
     * Salva as respostas de um formulário de satisfação enviado por um hóspede.
     * Inclui validações para garantir a integridade dos dados.
     *
     * @param request DTO contendo todas as respostas do formulário.
     * @return O ID da resposta de formulário salva.
     * @throws ResourceNotFoundException Se a empresa, seção ou pergunta não forem encontrados.
     * @throws ValidationException se as respostas obrigatórias não forem fornecidas ou outras regras de negócio forem violadas.
     */
    @Transactional
    public Long submitSurveyResponse(SurveyResponseRequest request) {

        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));

        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setCompany(company);
        surveyResponse.setResponseDate(LocalDateTime.now());
        surveyResponse.setGuestIdentifier(request.getGuestIdentifier());
        surveyResponse.setFreeTextFeedback(request.getFreeTextFeedback());
        surveyResponse.setSerieEmpresa(company.getSerieEmpresa());
        surveyResponse.setLanguage(request.getLanguage()); // <-- Adicione esta linha

        SurveyResponse savedSurveyResponse = surveyResponseRepository.save(surveyResponse);

        List<QuestionAnswer> answers = new ArrayList<>();
        Map<Long, SurveySection> activeSections = new HashMap<>();
        Map<Long, Question> activeQuestions = new HashMap<>();


        List<SurveySection> sectionsForCompany = surveySectionRepository.findByCompanyIdAndActiveTrueWithQuestions(company.getId());
        for (SurveySection section : sectionsForCompany) {
            activeSections.put(section.getId(), section);
            for (Question question : section.getQuestions()) {
                activeQuestions.put(question.getId(), question);
            }
        }

        for (QuestionAnswerRequest answerRequest : request.getAnswers()) {
            SurveySection surveySection = activeSections.get(answerRequest.getSurveySectionId());
            if (surveySection == null) {
                throw new ValidationException("Survey section with ID " + answerRequest.getSurveySectionId() + " is not active or does not exist for the given company/language.");
            }

            Question question = activeQuestions.get(answerRequest.getQuestionId());
            if (question == null || !Objects.equals(question.getSurveySection().getId(), surveySection.getId())) {
                throw new ValidationException("Question with ID " + answerRequest.getQuestionId() + " is not active, does not exist, or does not belong to section " + answerRequest.getSurveySectionId());
            }

            if (!Boolean.TRUE.equals(question.getDeniable()) && Boolean.TRUE.equals(answerRequest.getDidNotUseService())) {
                throw new ValidationException("Question '" + question.getLabel() + "' cannot be marked as 'did not use service'. It requires a direct response.");
            }

            if (Boolean.TRUE.equals(question.getMandatory()) && !Boolean.TRUE.equals(answerRequest.getDidNotUseService()) && !StringUtils.hasText(answerRequest.getAnswerValue())) {
                throw new ValidationException("Mandatory question '" + question.getLabel() + "' requires an answer.");
            }

            if (Boolean.TRUE.equals(answerRequest.getDidNotUseService()) && StringUtils.hasText(answerRequest.getAnswerValue())) {
                throw new ValidationException("You cannot provide an answer to a question marked as 'did not use service'. Answer value must be blank.");
            }

            if (!Boolean.TRUE.equals(answerRequest.getDidNotUseService())) {
                if (question.getType() == QuestionType.SCALE) {
                    try {
                        int rating = Integer.parseInt(answerRequest.getAnswerValue());
                        if (rating < 1 || rating > 5) {
                            throw new ValidationException("Scale question '" + question.getLabel() + "' must have a value between 1 and 5.");
                        }
                    } catch (NumberFormatException e) {
                        throw new ValidationException("Scale question '" + question.getLabel() + "' requires a numeric answer.");
                    }
                } else if (question.getType() == QuestionType.YES_NO) {
                    if (!"true".equalsIgnoreCase(answerRequest.getAnswerValue()) && !"false".equalsIgnoreCase(answerRequest.getAnswerValue())) {
                        throw new ValidationException("Yes/No question '" + question.getLabel() + "' must be 'true' or 'false'.");
                    }
                } else if (question.getType() == QuestionType.CHOICE) {
                    if (question.getOptions() != null) {
                        List<String> validOptions = question.getOptions();
                        if (!validOptions.contains(answerRequest.getAnswerValue())) {
                            throw new ValidationException("Choice question '" + question.getLabel() + "' has an invalid answer value. Valid options are: " + String.join(", ", validOptions));
                        }
                    } else {
                        throw new ValidationException("Choice question '" + question.getLabel() + "' has no valid options defined.");
                    }
                }
            }

            QuestionAnswer answer = new QuestionAnswer();
            answer.setSurveyResponse(savedSurveyResponse);
            answer.setQuestion(question);
            answer.setSurveySection(surveySection);
            answer.setAnswerValue(answerRequest.getAnswerValue());
            answer.setDidNotUseService(answerRequest.getDidNotUseService());
            answers.add(answer);
        }

        questionAnswerRepository.saveAll(answers);
        return savedSurveyResponse.getId();
    }
    // novo método para buscar detalhes
    public SurveyResponseDetailDto getSurveyResponseById(Long id) {
        SurveyResponse entity = surveyResponseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SurveyResponse not found with ID: " + id));

        SurveyResponseDetailDto dto = new SurveyResponseDetailDto();
        dto.setId(entity.getId());
        dto.setCompanyId(entity.getCompany().getId());
        dto.setCompanyName(entity.getCompany().getName());
        dto.setLanguage(entity.getLanguage());
        dto.setSerieEmpresa(entity.getCompany().getSerieEmpresa());
        dto.setResponseDate(entity.getResponseDate());
        dto.setGuestIdentifier(entity.getGuestIdentifier());
        dto.setFreeTextFeedback(entity.getFreeTextFeedback());

        //  mapper para answers
        List<QuestionAnswerDetailDto> answersDto = entity.getAnswers().stream().map(answer -> {
            QuestionAnswerDetailDto dtoDetail = new QuestionAnswerDetailDto();
            dtoDetail.setAnswerId(answer.getId());
            dtoDetail.setQuestionId(answer.getQuestion().getId());
            dtoDetail.setQuestionLabel(answer.getQuestion().getLabel());
            dtoDetail.setQuestionType(answer.getQuestion().getType());
            dtoDetail.setSurveySectionId(answer.getSurveySection().getId());
            dtoDetail.setSurveySectionName(answer.getSurveySection().getName());
            dtoDetail.setAnswerValue(answer.getAnswerValue());
            dtoDetail.setDidNotUseService(answer.getDidNotUseService());
            return dtoDetail;
        }).toList();

        dto.setAnswers(answersDto);

        return dto;
    }
}