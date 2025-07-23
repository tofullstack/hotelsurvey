package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionAnswerRequest;
import br.com.survey.hotelsurvey.dto.SurveyResponseRequest;
import br.com.survey.hotelsurvey.entity.*;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils; // Certifique-se de que este import está lá

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
        // 1. Validar e buscar a Company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));

        // 2. Vai criar a entidade SurveyResponse
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setCompany(company);
        surveyResponse.setLanguage(request.getLanguage());
        surveyResponse.setResponseDate(LocalDateTime.now());
        surveyResponse.setGuestIdentifier(request.getGuestIdentifier());
        surveyResponse.setFreeTextFeedback(request.getFreeTextFeedback());

        SurveyResponse savedSurveyResponse = surveyResponseRepository.save(surveyResponse);

        List<QuestionAnswer> answers = new ArrayList<>();
        // Mapa para armazenar as seções de pesquisa ativas e perguntas ativas para validação eficiente
        Map<Long, SurveySection> activeSections = new HashMap<>();
        Map<Long, Question> activeQuestions = new HashMap<>();

        // Pré-carregar todas as seções e perguntas ativas para a empresa e idioma
        List<SurveySection> sectionsForCompanyAndLanguage = surveySectionRepository.findByCompanyIdAndLanguageAndActiveTrue(company.getId(), request.getLanguage());
        for (SurveySection section : sectionsForCompanyAndLanguage) {
            activeSections.put(section.getId(), section);
            // Certifique-se de que as perguntas são carregadas com a seção
            for (Question question : section.getQuestions()) {
                activeQuestions.put(question.getId(), question);
            }
        }

        // Processar e validar cada resposta individual
        for (QuestionAnswerRequest answerRequest : request.getAnswers()) {
            SurveySection surveySection = activeSections.get(answerRequest.getSurveySectionId());
            if (surveySection == null) {
                throw new ValidationException("Survey section with ID " + answerRequest.getSurveySectionId() + " is not active or does not exist for the given company/language.");
            }

            Question question = activeQuestions.get(answerRequest.getQuestionId());
            if (question == null || !Objects.equals(question.getSurveySection().getId(), surveySection.getId())) {
                throw new ValidationException("Question with ID " + answerRequest.getQuestionId() + " is not active, does not exist, or does not belong to section " + answerRequest.getSurveySectionId());
            }

            // --- INÍCIO DAS VALIDAÇÕES AJUSTADAS APÓS REMOVER @NotBlank DO DTO ---

            // Validação 1: Não permitir 'didNotUseService' se a pergunta não for deniável
            if (!question.getDeniable() && answerRequest.getDidNotUseService()) {
                throw new ValidationException("Question '" + question.getLabel() + "' cannot be marked as 'did not use service'. It requires a direct response.");
            }

            // Validação 2: Se a pergunta é obrigatória E o serviço FOI USADO, o valor da resposta NÃO PODE estar em branco.
            if (question.getMandatory() && !answerRequest.getDidNotUseService() && !StringUtils.hasText(answerRequest.getAnswerValue())) {
                throw new ValidationException("Mandatory question '" + question.getLabel() + "' requires an answer.");
            }

            // Validação 3: Se o serviço NÃO FOI USADO, o valor da resposta DEVE estar em branco.
            // Isso evita que o usuário preencha algo e marque "não usei".
            if (answerRequest.getDidNotUseService() && StringUtils.hasText(answerRequest.getAnswerValue())) {
                throw new ValidationException("You cannot provide an answer to a question marked as 'did not use service'. Answer value must be blank.");
            }

            // --- FIM DAS VALIDAÇÕES AJUSTADAS ---


            // Validações específicas por tipo de pergunta, só se o serviço FOI UTILIZADO
            if (!answerRequest.getDidNotUseService()) {
                if (question.getType() == QuestionType.SCALE) {
                    try {
                        int rating = Integer.parseInt(answerRequest.getAnswerValue());
                        if (rating < 1 || rating > 5) { // Exemplo de range. Ajuste conforme suas opções.
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
                        // Se for CHOICE, mas não tem opções definidas, talvez seja um erro na definição do formulário
                        throw new ValidationException("Choice question '" + question.getLabel() + "' has no valid options defined.");
                    }
                }
                // Para TEXT, a validação de `NotBlank` já foi feita acima se for mandatório.
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
}