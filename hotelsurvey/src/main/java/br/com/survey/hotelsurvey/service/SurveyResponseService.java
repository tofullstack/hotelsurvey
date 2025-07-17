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
     * salva as respostas de um formulário de satisfação enviado por um hóspede.
     * inclui validações para garantir a integridade dos dados.
     *
     * @param request DTO contendo todas as respostas do formulário.
     * @return O ID da resposta de formulário salva.
     * @throws ResourceNotFoundException Se a empresa, seção ou pergunta não forem encontrados.
     * @throws ValidationException se as respostas obrigatórias não forem fornecidas ou outras regras de negócio forem violadas.
     */
    @Transactional
    public Long submitSurveyResponse(SurveyResponseRequest request) {
        // 1. validar e buscar a Company
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));

        // 2. vai criar a entidade SurveyResponse
        SurveyResponse surveyResponse = new SurveyResponse();
        surveyResponse.setCompany(company);
        surveyResponse.setLanguage(request.getLanguage());
        surveyResponse.setResponseDate(LocalDateTime.now());
        surveyResponse.setGuestIdentifier(request.getGuestIdentifier());
        surveyResponse.setFreeTextFeedback(request.getFreeTextFeedback());

        SurveyResponse savedSurveyResponse = surveyResponseRepository.save(surveyResponse);

        List<QuestionAnswer> answers = new ArrayList<>();
        // mapa para armazenar as seções de pesquisa ativas e suas perguntas ativas para validação eficiente
        Map<Long, SurveySection> activeSections = new HashMap<>();
        Map<Long, Question> activeQuestions = new HashMap<>();

        // pré-carregar todas as seções e perguntas ativas para a empresa e idioma
        List<SurveySection> sectionsForCompanyAndLanguage = surveySectionRepository.findByCompanyIdAndLanguageAndActiveTrue(company.getId(), request.getLanguage());
        for (SurveySection section : sectionsForCompanyAndLanguage) {
            activeSections.put(section.getId(), section);
            for (Question question : section.getQuestions()) {
                activeQuestions.put(question.getId(), question);
            }
        }

        // 3. processar e validar cada resposta individual
        for (QuestionAnswerRequest answerRequest : request.getAnswers()) {
            SurveySection surveySection = activeSections.get(answerRequest.getSurveySectionId());
            if (surveySection == null) {
                throw new ValidationException("Survey section with ID " + answerRequest.getSurveySectionId() + " is not active or does not exist for the given company/language.");
            }

            Question question = activeQuestions.get(answerRequest.getQuestionId());
            if (question == null || !Objects.equals(question.getSurveySection().getId(), surveySection.getId())) {
                throw new ValidationException("Question with ID " + answerRequest.getQuestionId() + " is not active, does not exist, or does not belong to section " + answerRequest.getSurveySectionId());
            }

            // validação de pergunta obrigatória
            // se a pergunta é obrigatória E o serviço não foi negado && o valor da resposta está em branco
            if (question.getMandatory() && !answerRequest.getDidNotUseService() && !org.springframework.util.StringUtils.hasText(answerRequest.getAnswerValue())) {
                throw new ValidationException("Mandatory question '" + question.getLabel() + "' in section '" + surveySection.getName() + "' requires an answer.");
            }

            // validação para denyUse:
            // se a seção permite "denyUse" e o hóspede marcou que não utilizou,
            // então não deve haver valor na resposta da pergunta (ou o valor deve ser ignorado)
            if (surveySection.getDenyUse() && answerRequest.getDidNotUseService()) {
                // se marcou que não usou, a resposta da pergunta individual deve ser vazia ou nula
                answerRequest.setAnswerValue(null); // limpa o valor da resposta
            } else if (answerRequest.getDidNotUseService() && !surveySection.getDenyUse()) {
                // se marcou que não usou, mas a seção não permite "denyUse", isso é um erro
                throw new ValidationException("Section '" + surveySection.getName() + "' does not allow 'did not use service' option.");
            }


            // validações específicas por tipo de pergunta (ex: escala entre min/max, escolha dentro das opções)
            // para `TEXT`, `YES_NO` e `CHOICE` vai so verificar se não está vazio se for obrigatória (já coberto)
            // para `SCALE`, você precisaria de lógica adicional para validar o range (e.g., de 1 a 5)
            if (question.getType() == QuestionType.SCALE) {
                try {
                    int rating = Integer.parseInt(answerRequest.getAnswerValue());
                    // exemplo: se as opções são "1,2,3,4,5"
                    // você precisaria de um parser para as options e validar se o rating está no range.
                    // por simplicidade, assumimos que 1 a 5 é um range comum aqui.
                    if (rating < 1 || rating > 5) { // Exemplo de range, ajuste conforme seu caso
                        throw new ValidationException("Scale question '" + question.getLabel() + "' must have a value between 1 and 5.");
                    }
                } catch (NumberFormatException e) {
                    throw new ValidationException("Scale question '" + question.getLabel() + "' requires a numeric answer.");
                }
            } else if (question.getType() == QuestionType.YES_NO) {
                if (!"true".equalsIgnoreCase(answerRequest.getAnswerValue()) && !"false".equalsIgnoreCase(answerRequest.getAnswerValue())) {
                    throw new ValidationException("Yes/No question '" + question.getLabel() + "' must be 'true' or 'false'.");
                }
            }
            // para CHOICE, você precisaria de uma validação para garantir que o answerValue está entre as options da pergunta
            // question.getOptions() (ex: "Opção A,Opção B,Opção C")
            else if (question.getType() == QuestionType.CHOICE) {
                if (question.getOptions() != null) {
                    List<String> validOptions = question.getOptions();
                    if (!validOptions.contains(answerRequest.getAnswerValue())) {
                        throw new ValidationException("Choice question '" + question.getLabel() + "' has an invalid answer value.");
                    }
                }
            }


            QuestionAnswer answer = new QuestionAnswer();
            answer.setSurveyResponse(savedSurveyResponse);
            answer.setQuestion(question);
            answer.setSurveySection(surveySection); // associa a seção também
            answer.setAnswerValue(answerRequest.getAnswerValue());
            answer.setDidNotUseService(answerRequest.getDidNotUseService());
            answers.add(answer);
        }

        questionAnswerRepository.saveAll(answers);
        return savedSurveyResponse.getId();
    }
}