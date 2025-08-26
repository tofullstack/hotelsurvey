package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.ConditionalTriggerRequest;
import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.QuestionTranslationDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.*;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.ConditionalSectionTriggerRepository;
import br.com.survey.hotelsurvey.repository.QuestionRepository;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FormAdminService {

    @Autowired
    private SurveySectionRepository surveySectionRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ConditionalSectionTriggerRepository triggerRepository;

    @Transactional
    public SurveySectionDto createForm(SurveySectionDto dto) {
        if (surveySectionRepository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())) {
            throw new DuplicateEntryException("A form with this name and company already exists.");
        }

        validateQuestions(dto.getQuestions());

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        SurveySection surveySection = new SurveySection();
        surveySection.setName(dto.getName());
        surveySection.setCompany(company);
        surveySection.setActive(dto.getActive());
        surveySection.setLanguage(dto.getLanguage());
        surveySection.setSerieEmpresa(dto.getSerieEmpresa());
        surveySection.setConditional(dto.getConditional());

        SurveySection savedSection = surveySectionRepository.save(surveySection);

        List<Question> questions = dto.getQuestions().stream()
                .map(qDto -> convertToQuestionEntity(qDto, savedSection,dto.getLanguage()))
                .collect(Collectors.toList());
        questionRepository.saveAll(questions);
        savedSection.setQuestions(questions);

        if (dto.getTriggers() != null && !dto.getTriggers().isEmpty()) {
            List<ConditionalSectionTrigger> triggers = dto.getTriggers().stream()
                    .map(tDto -> {
                        int questionIndex = tDto.getQuestionId().intValue();
                        if (questionIndex < 0 || questionIndex >= questions.size()) {
                            throw new ValidationException("Question index for trigger is out of bounds: " + tDto.getQuestionId());
                        }
                        Question question = questions.get(questionIndex);
                        return convertToTriggerEntity(tDto, question);
                    })
                    .collect(Collectors.toList());
            triggerRepository.saveAll(triggers);
        }

        return convertToSurveySectionDto(savedSection);
    }

    @Transactional
    public SurveySectionDto updateForm(Long id, SurveySectionDto dto) {
        SurveySection existingSection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));

        if (!existingSection.getName().equals(dto.getName()) || !existingSection.getCompany().getId().equals(dto.getCompanyId())) {
            if (surveySectionRepository.existsByNameAndCompanyId(dto.getName(), dto.getCompanyId())) {
                throw new DuplicateEntryException("A form with this name and company already exists.");
            }
        }
        validateQuestions(dto.getQuestions());

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        // atualiza os campos principais do formulário
        existingSection.setName(dto.getName());
        existingSection.setCompany(company);
        existingSection.setActive(dto.getActive());
        existingSection.setLanguage(dto.getLanguage());
        existingSection.setSerieEmpresa(dto.getSerieEmpresa());
        existingSection.setConditional(dto.getConditional());

        triggerRepository.deleteAllByQuestionSurveySectionId(existingSection.getId());

        List<Question> newQuestionsEntities = dto.getQuestions().stream()
                .map(qDto -> convertToQuestionEntity(qDto, existingSection, dto.getLanguage()))
                .collect(Collectors.toList());

        existingSection.getQuestions().clear();
        existingSection.getQuestions().addAll(newQuestionsEntities);

        SurveySection updatedSection = surveySectionRepository.save(existingSection);

        if (dto.getTriggers() != null && !dto.getTriggers().isEmpty()) {
            Map<Long, Long> tempIdToRealIdMap = new HashMap<>();
            for (Question question : updatedSection.getQuestions()) {
                dto.getQuestions().stream()
                        .filter(qDto -> qDto.getId() != null && qDto.getId().toString().startsWith("temp-"))
                        .filter(qDto -> qDto.getLabel().equals(question.getLabel())) // Assumindo que o label é único
                        .findFirst()
                        .ifPresent(qDto -> tempIdToRealIdMap.put(Long.valueOf(qDto.getId().toString().replace("temp-", "")), question.getId()));
            }

            List<ConditionalSectionTrigger> newTriggers = dto.getTriggers().stream()
                    .map(tDto -> {
                        Question question;
                        Long questionIdFromDto = tDto.getQuestionId();
                        // se for um ID temporário, usa o mapa para encontrar o ID real
                        if (tDto.getQuestionId() != null && tDto.getQuestionId().toString().startsWith("temp-")) {
                            Long originalTempId = Long.valueOf(tDto.getQuestionId().toString().replace("temp-", ""));
                            Long realId = tempIdToRealIdMap.get(originalTempId);
                            if (realId == null) {
                                throw new ValidationException("Temporal ID mapping failed for question: " + tDto.getQuestionId());
                            }
                            question = questionRepository.findById(realId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with real ID: " + realId));
                        } else {
                            // se for um ID real, busca a pergunta diretamente
                            question = questionRepository.findById(questionIdFromDto)
                                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionIdFromDto));
                        }
                        return convertToTriggerEntity(tDto, question);
                    })
                    .collect(Collectors.toList());
            triggerRepository.saveAll(newTriggers);
        }

        return convertToSurveySectionDto(updatedSection);
    }
    @Transactional
    public void deactivateForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        surveySection.setActive(false);
        surveySectionRepository.save(surveySection);
    }

    @Transactional
    public void activateForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        surveySection.setActive(true);
        surveySectionRepository.save(surveySection);
    }

    public SurveySectionDto getFormById(Long id) {
        return surveySectionRepository.findById(id)
                .map(this::convertToSurveySectionDto)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
    }

    public List<SurveySectionDto> getAllForms() {
        return surveySectionRepository.findAll().stream()
                .map(this::convertToSurveySectionDto)
                .collect(Collectors.toList());
    }

    public SurveySectionDto previewForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        return convertToSurveySectionDto(surveySection);
    }

    public List<SurveySectionDto> getConditionalFormsForCompany(Long companyId) {
        return surveySectionRepository.findByConditionalIsTrueAndCompanyId(companyId)
                .stream()
                .map(this::convertToSurveySectionDto)
                .collect(Collectors.toList());
    }

    private void validateQuestions(List<QuestionDto> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new ValidationException("A survey form must have at least one question.");
        }

        Set<String> distinctLabels = new HashSet<>();

        for (QuestionDto qDto : questions) {
            if (qDto.getTranslations() == null || qDto.getTranslations().isEmpty()) {
                throw new ValidationException("Each question must have at least one translation.");
            }

            for (QuestionTranslationDto translation : qDto.getTranslations()) {
                if (!StringUtils.hasText(translation.getLabel())) {
                    throw new ValidationException("Each question must have at least one non-empty translation label.");
                }
                if (!StringUtils.hasText(translation.getLanguage())) {
                    throw new ValidationException("Each translation must have a language.");
                }
            }

            String mainLabel = qDto.getTranslations().stream()
                    .map(QuestionTranslationDto::getLabel)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Each question must have at least one non-empty translation label."));

            if (!distinctLabels.add(mainLabel.trim().toLowerCase())) {
                throw new ValidationException("Duplicate question labels (considering their primary translation) are not allowed: " + mainLabel);
            }

            Set<String> translationKeys = new HashSet<>();
            for (QuestionTranslationDto translation : qDto.getTranslations()) {
                String key = translation.getLanguage().trim().toLowerCase() + "::" + translation.getLabel().trim().toLowerCase();
                if (!translationKeys.add(key)) {
                    throw new ValidationException("Duplicate translation within a question: " + translation.getLabel() + " [" + translation.getLanguage() + "]");
                }
            }
        }
    }

    private SurveySectionDto convertToSurveySectionDto(SurveySection entity) {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setSerieEmpresa(entity.getSerieEmpresa());
        dto.setConditional(entity.getConditional());
        dto.setLanguage(entity.getLanguage());
        if (entity.getCompany() != null) {
            dto.setCompanyId(entity.getCompany().getId());
            dto.setCompanyName(entity.getCompany().getName());
        } else {
            dto.setCompanyId(null);
            dto.setCompanyName(null);
        }

        dto.setActive(entity.getActive());

        dto.setQuestions(entity.getQuestions().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList()));

        List<ConditionalTriggerRequest> allTriggers = entity.getQuestions().stream()
                .flatMap(q -> q.getTriggers().stream())
                .map(t -> {
                    ConditionalTriggerRequest tDto = new ConditionalTriggerRequest();
                    tDto.setQuestionId(t.getQuestion().getId());
                    tDto.setTargetSectionId(t.getTargetSection().getId());
                    tDto.setTriggerValue(t.getTriggerValue());
                    return tDto;
                })
                .collect(Collectors.toList());
        dto.setTriggers(allTriggers);

        return dto;
    }

    private Question convertToQuestionEntity(QuestionDto dto, SurveySection surveySection, String formLanguage) {
        Question entity = new Question();

        if (dto.getId() != null && !dto.getId().toString().startsWith("temp-")) {
            entity.setId(dto.getId());
        }

        entity.setSurveySection(surveySection);


        String primaryLabel = dto.getLabel();
        entity.setLabel(primaryLabel);
        entity.setType(dto.getType());
        entity.setMandatory(dto.getMandatory());
        entity.setDeniable(dto.getDeniable());
        entity.setOptions(dto.getOptions());

        entity.setTranslations(
                dto.getTranslations().stream()
                        .filter(t -> !formLanguage.equals(t.getLanguage()))
                        .map(t -> {
                            var qt = new QuestionTranslation();
                            qt.setLabel(t.getLabel());
                            qt.setLanguage(t.getLanguage());
                            qt.setQuestion(entity);
                            return qt;
                        }).collect(Collectors.toSet())
        );


        var primaryTranslation = new QuestionTranslation();
        primaryTranslation.setLabel(primaryLabel);
        primaryTranslation.setLanguage(formLanguage);
        primaryTranslation.setQuestion(entity);
        entity.getTranslations().add(primaryTranslation);

        return entity;
    }
    private QuestionDto convertToQuestionDto(Question entity) {
        QuestionDto dto = new QuestionDto();
        dto.setId(entity.getId());
        dto.setSurveySectionId(entity.getSurveySection().getId());
        dto.setType(entity.getType());
        dto.setLabel(entity.getLabel());
        dto.setMandatory(entity.getMandatory());
        dto.setDeniable(entity.getDeniable());
        dto.setOptions(entity.getOptions());
        dto.setTranslations(
                entity.getTranslations().stream().map(t -> {
                    var tDto = new QuestionTranslationDto();
                    tDto.setLabel(t.getLabel());
                    tDto.setLanguage(t.getLanguage());
                    return tDto;
                }).collect(Collectors.toList())
        );
        return dto;
    }

    private ConditionalSectionTrigger convertToTriggerEntity(ConditionalTriggerRequest dto, Question question) {
        ConditionalSectionTrigger trigger = new ConditionalSectionTrigger();
        trigger.setTriggerValue(dto.getTriggerValue());
        trigger.setQuestion(question);

        SurveySection targetSection = surveySectionRepository.findById(dto.getTargetSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Target section not found for trigger with ID: " + dto.getTargetSectionId()));
        trigger.setTargetSection(targetSection);

        return trigger;
    }

    public List<SurveySectionDto> searchForms(String companyName, String status) {
        List<SurveySection> forms = surveySectionRepository.findAll();
        return forms.stream()
                .filter(f -> companyName == null || f.getCompany().getName().toLowerCase().contains(companyName.toLowerCase()))
                .filter(f -> {
                    if ("ativos".equalsIgnoreCase(status)) return Boolean.TRUE.equals(f.getActive());
                    if ("inativos".equalsIgnoreCase(status)) return Boolean.FALSE.equals(f.getActive());
                    return true;
                })
                .map(this::convertToSurveySectionDto)
                .collect(Collectors.toList());
    }

    public SurveySectionDto getFormWithTranslatedQuestions(Long id, String targetLanguage) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));

        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(surveySection.getId());
        dto.setName(surveySection.getName());
        dto.setCompanyId(surveySection.getCompany() != null ? surveySection.getCompany().getId() : null);
        dto.setCompanyName(surveySection.getCompany() != null ? surveySection.getCompany().getName() : null);
        dto.setActive(surveySection.getActive());
        dto.setSerieEmpresa(surveySection.getSerieEmpresa());
        dto.setConditional(surveySection.getConditional());

        List<QuestionDto> translatedQuestions = surveySection.getQuestions().stream()
                .map(questionEntity -> {
                    QuestionDto qDto = new QuestionDto();
                    qDto.setId(questionEntity.getId());
                    qDto.setSurveySectionId(questionEntity.getSurveySection().getId());
                    qDto.setType(questionEntity.getType());
                    qDto.setDeniable(questionEntity.getDeniable());
                    qDto.setMandatory(questionEntity.getMandatory());
                    qDto.setOptions(questionEntity.getOptions());

                    String finalQuestionLabel = questionEntity.getTranslations().stream()
                            .filter(t -> targetLanguage.equalsIgnoreCase(t.getLanguage()))
                            .map(QuestionTranslation::getLabel)
                            .findFirst()
                            .orElseGet(() -> questionEntity.getTranslations().stream()
                                    .filter(t -> "pt-BR".equalsIgnoreCase(t.getLanguage()))
                                    .map(QuestionTranslation::getLabel)
                                    .findFirst()
                                    .orElse(questionEntity.getLabel()));
                    qDto.setLabel(finalQuestionLabel);
                    qDto.setTranslations(Collections.emptyList());

                    return qDto;
                })
                .collect(Collectors.toList());
        dto.setQuestions(translatedQuestions);
        return dto;
    }
}