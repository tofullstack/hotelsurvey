package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.QuestionTranslationDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.*;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.QuestionRepository;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Cria um novo formulário de satisfação.
     * Cada formulário deverá ter um nome único no sistema.
     * Não será permitido duplicidade de nome, mesmo que o formulário esteja desativado.
     *
     * @param dto DTO contendo os dados do formulário a ser criado.
     * @return SurveySectionDto do formulário criado.
     * @throws DuplicateEntryException Se já existir um formulário com o mesmo nome, idioma e empresa.
     * @throws ValidationException Se as perguntas estiverem inválidas (vazias ou duplicadas).
     * @throws ResourceNotFoundException Se a empresa não for encontrada.
     */
    @Transactional
    public SurveySectionDto createForm(SurveySectionDto dto) {
        // valida nome único por empresa/idioma
        if (surveySectionRepository.existsByNameAndLanguageAndCompanyId(dto.getName(), dto.getLanguage(), dto.getCompanyId())) {
            throw new DuplicateEntryException("A form with this name, language, and company already exists.");
        }

        validateQuestions(dto.getQuestions()); // valida perguntas (não vazias, não duplicadas)

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        SurveySection surveySection = new SurveySection();
        surveySection.setName(dto.getName());
//        surveySection.setDenyUse(dto.getDenyUse());
        surveySection.setLanguage(dto.getLanguage());
        surveySection.setCompany(company);
        surveySection.setActive(dto.getActive());

        SurveySection savedSection = surveySectionRepository.save(surveySection);

        // associa e salva as perguntas
        List<Question> questions = dto.getQuestions().stream()
                .map(qDto -> convertToQuestionEntity(qDto, savedSection))
                .collect(Collectors.toList());
        questionRepository.saveAll(questions); // salva as perguntas associadas
        savedSection.setQuestions(questions); // atualiza a lista na entidade em memória

        return convertToSurveySectionDto(savedSection);
    }

    /**
     * Atualiza um formulário de satisfação existente.
     *
     * @param id ID do formulário a ser atualizado.
     * @param dto DTO contendo os novos dados do formulário.
     * @return SurveySectionDto do formulário atualizado.
     * @throws ResourceNotFoundException Se o formulário não for encontrado.
     * @throws DuplicateEntryException Se a atualização resultar em um nome duplicado.
     * @throws ValidationException Se as perguntas estiverem inválidas.
     */
    @Transactional
    public SurveySectionDto updateForm(Long id, SurveySectionDto dto) {
        SurveySection existingSection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));

        // valida nome único para atualizações
        // verifica se houve mudança no nome, idioma ou empresa para revalidar a unicidade
        if (!existingSection.getName().equals(dto.getName()) ||
                !existingSection.getLanguage().equals(dto.getLanguage()) ||
                !existingSection.getCompany().getId().equals(dto.getCompanyId())) {
            if (surveySectionRepository.existsByNameAndLanguageAndCompanyId(dto.getName(), dto.getLanguage(), dto.getCompanyId())) {
                throw new DuplicateEntryException("A form with this name, language, and company already exists.");
            }
        }

        validateQuestions(dto.getQuestions()); // Valida perguntas

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        existingSection.setName(dto.getName());
//        existingSection.setDenyUse(dto.getDenyUse());
        existingSection.setLanguage(dto.getLanguage());
        existingSection.setCompany(company);
        existingSection.setActive(dto.getActive());

        // Atualiza as perguntas: remove as antigas, adiciona as novas/atualizadas
        // Isso depende do CascadeType.ALL e orphanRemoval=true na relação @OneToMany em SurveySection
        existingSection.getQuestions().clear();
        dto.getQuestions().forEach(qDto -> existingSection.getQuestions().add(convertToQuestionEntity(qDto, existingSection)));


        SurveySection updatedSection = surveySectionRepository.save(existingSection); // Salva a seção e suas perguntas
        return convertToSurveySectionDto(updatedSection);
    }

    /**
     * Desativa um formulário de satisfação (soft delete).
     * Formulários desativados não aparecem na API pública, mas os dados são preservados.
     * @param id ID do formulário a ser desativado.
     * @throws ResourceNotFoundException Se o formulário não for encontrado.
     */
    @Transactional
    public void deactivateForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        surveySection.setActive(false);
        surveySectionRepository.save(surveySection);
    }

    /**
     * Ativa um formulário de satisfação desativado.
     * @param id ID do formulário a ser ativado.
     * @throws ResourceNotFoundException Se o formulário não for encontrado.
     */
    @Transactional
    public void activateForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        surveySection.setActive(true);
        surveySectionRepository.save(surveySection);
    }

    /**
     * Retorna um formulário de satisfação pelo ID.
     * @param id ID do formulário.
     * @return SurveySectionDto do formulário.
     * @throws ResourceNotFoundException Se o formulário não for encontrado.
     */
    public SurveySectionDto getFormById(Long id) {
        return surveySectionRepository.findById(id)
                .map(this::convertToSurveySectionDto)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
    }

    /**
     * Retorna a lista de todos os formulários cadastrados.
     * @return Lista de SurveySectionDto.
     */
    public List<SurveySectionDto> getAllForms() {
        return surveySectionRepository.findAll().stream()
                .map(this::convertToSurveySectionDto)
                .collect(Collectors.toList());
    }

    /**
     * Retorna uma pré-visualização de um formulário.
     * O usuário poderá realizar uma pré-visualização do formulário antes de ativá-lo.
     * Para a pré-visualização, o status 'active' do formulário não é relevante.
     * @param id ID do formulário para pré-visualizar.
     * @return SurveySectionDto da pré-visualização.
     * @throws ResourceNotFoundException Se o formulário não for encontrado.
     */
    public SurveySectionDto previewForm(Long id) {
        SurveySection surveySection = surveySectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + id));
        return convertToSurveySectionDto(surveySection);
    }

    /**
     * Valida a lista de perguntas de um formulário.
     * Impede cadastro de perguntas em branco.
     * Impede cadastro de perguntas duplicadas no mesmo formulário.
     * @param questions Lista de QuestionDto para validação.
     * @throws ValidationException Se as perguntas forem inválidas.
     */
    private void validateQuestions(List<QuestionDto> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new ValidationException("A survey form must have at least one question.");
        }

        Set<String> distinctLabels = new HashSet<>();
        for (QuestionDto qDto : questions) {
            // pega a tradução principal (pt-BR ou a primeira da lista)
            String mainLabel = qDto.getTranslations().stream()
                    .filter(t -> "pt-BR".equalsIgnoreCase(t.getLanguage()))
                    .map(t -> t.getLabel())
                    .findFirst()
                    .orElseThrow(() -> new ValidationException("Each question must have at least one translation in 'pt-BR'."));

            if (!org.springframework.util.StringUtils.hasText(mainLabel)) {
                throw new ValidationException("Question translation label cannot be blank.");
            }

            // impede duplicadas com base na label traduzida
            if (!distinctLabels.add(mainLabel.trim().toLowerCase())) {
                throw new ValidationException("Duplicate question labels (in pt-BR) are not allowed: " + mainLabel);
            }
        }
    }


    // helper para converter entidade em DTO
    private SurveySectionDto convertToSurveySectionDto(SurveySection entity) {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
//        dto.setDenyUse(entity.getDenyUse());
        dto.setLanguage(entity.getLanguage());
        //garante que a company não é nula antes de acessar o ID
        dto.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);
        dto.setActive(entity.getActive());
        dto.setQuestions(entity.getQuestions().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList()));
        return dto;
    }

    private QuestionDto convertToQuestionDto(Question entity) {
        QuestionDto dto = new QuestionDto();
        dto.setId(entity.getId());
        dto.setType(entity.getType().name()); // pega o nome do enum como String para o DTO
        dto.setMandatory(entity.getMandatory());
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

    private Question convertToQuestionEntity(QuestionDto dto, SurveySection surveySection) {
        Question entity = new Question();
        entity.setId(dto.getId());
        entity.setSurveySection(surveySection);

        // encontra a primeira tradução disponível para usar como label principal.
        // se não houver traduções, usa um texto padrão.
        String primaryLabel = dto.getTranslations().stream()
                .map(t -> t.getLabel()) // napeia para o label da tradução
                .filter(org.springframework.util.StringUtils::hasText) // filtra labels não vazios
                .findFirst() // Pega o primeiro
                .orElse("Default Question Label"); //sSe nenhuma tradução ou todas vazias, usa este fallback

        entity.setLabel(primaryLabel); // <--- preenche o campo 'label' da entidade Question

        entity.setType(QuestionType.valueOf(dto.getType().toUpperCase()));
        entity.setMandatory(dto.isMandatory());
        entity.setOptions(dto.getOptions());

        // converte as traduções para Set
        entity.setTranslations(
                dto.getTranslations().stream().map(t -> {
                    var qt = new QuestionTranslation();
                    qt.setLabel(t.getLabel());
                    qt.setLanguage(t.getLanguage());
                    qt.setQuestion(entity); // garante a referência de volta para a questão
                    return qt;
                }).collect(Collectors.toSet()) // coleta para um Set
        );

        return entity;
    }



    // método para filtro de pesquisa de formularios baseado em nome da empresa e status
    public List<SurveySectionDto> searchForms(String companyName, String status){
        List<SurveySection> forms= surveySectionRepository.findAll();
         return forms.stream()
                 .filter(f -> companyName == null || f.getCompany().getName().toLowerCase().contains(companyName.toLowerCase()))
                 .filter(f-> {
                     if("ativos".equalsIgnoreCase(status)) return Boolean.TRUE.equals(f.getActive());
                     if("inativos".equalsIgnoreCase(status)) return Boolean.FALSE.equals(f.getActive());
                     return true;
                 })
                 .map(this::convertToSurveySectionDto)
                 .collect(Collectors.toList());
    }


    private QuestionDto convertQuestionToDtoWithPreferredTranslation(Question question, String preferredLanguage) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setType(question.getType().name()); // converte o Enum QuestionType para String
        dto.setMandatory(question.getMandatory());
        dto.setOptions(question.getOptions());

        // Lógica para encontrar o label traduzido:
        // 1. Tenta encontrar a tradução para o idioma preferencial.
        // 2. Se não encontrar, tenta encontrar a tradução em pt-BR.
        // 3. Se ainda não encontrar, pega a primeira tradução disponível.
        // 4. Se não houver nenhuma tradução, usa um texto padrão.
        String selectedLabel = question.getTranslations().stream()
                .filter(t -> t.getLanguage().equalsIgnoreCase(preferredLanguage))
                .map(QuestionTranslation::getLabel)
                .findFirst()
                .orElseGet(() -> question.getTranslations().stream()
                        .filter(t -> "pt-BR".equalsIgnoreCase(t.getLanguage()))
                        .map(QuestionTranslation::getLabel)
                        .findFirst()
                        .orElseGet(() -> question.getTranslations().stream()
                                .map(QuestionTranslation::getLabel)
                                .findFirst()
                                .orElse("No translation available (" + preferredLanguage + ")"))); // Fallback final

        // Cria uma lista de QuestionTranslationDto com apenas a tradução selecionada.
        // Isso é necessário porque o QuestionDto espera uma List<QuestionTranslationDto>.
        QuestionTranslationDto singleTranslationDto = new QuestionTranslationDto();
        singleTranslationDto.setLanguage(preferredLanguage); // Pode ser o idioma da tradução encontrada, ou o preferencial
        singleTranslationDto.setLabel(selectedLabel);
        dto.setTranslations(Collections.singletonList(singleTranslationDto));

        return dto;
    }


    public SurveySectionDto getFormWithTranslatedQuestions(Long formId, String language) {
        // 1. Busca o formulário pela ID
        SurveySection surveySection = surveySectionRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Form not found with ID: " + formId));

        // 2. Mapeia a SurveySection para SurveySectionDto
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(surveySection.getId());
        dto.setName(surveySection.getName());
        dto.setLanguage(surveySection.getLanguage()); // Idioma original de criação do formulário
        dto.setCompanyId(surveySection.getCompany() != null ? surveySection.getCompany().getId() : null);
        dto.setActive(surveySection.getActive());

        // 3. Processa cada pergunta para obter a tradução correta
        List<QuestionDto> translatedQuestions = surveySection.getQuestions().stream()
                .map(question -> convertQuestionToDtoWithPreferredTranslation(question, language))
                .collect(Collectors.toList());

        dto.setQuestions(translatedQuestions);

        return dto;
    }
}
