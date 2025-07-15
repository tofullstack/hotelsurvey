package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.QuestionRepository;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
        // Valida nome único por empresa/idioma
        if (surveySectionRepository.existsByNameAndLanguageAndCompanyId(dto.getName(), dto.getLanguage(), dto.getCompanyId())) {
            throw new DuplicateEntryException("A form with this name, language, and company already exists.");
        }

        validateQuestions(dto.getQuestions()); // Valida perguntas (não vazias, não duplicadas)

        Company company = companyRepository.findById(dto.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + dto.getCompanyId()));

        SurveySection surveySection = new SurveySection();
        surveySection.setName(dto.getName());
        surveySection.setDenyUse(dto.getDenyUse());
        surveySection.setLanguage(dto.getLanguage());
        surveySection.setCompany(company);
        surveySection.setActive(dto.getActive());

        SurveySection savedSection = surveySectionRepository.save(surveySection);

        // Associa e salva as perguntas
        List<Question> questions = dto.getQuestions().stream()
                .map(qDto -> convertToQuestionEntity(qDto, savedSection))
                .collect(Collectors.toList());
        questionRepository.saveAll(questions); // Salva as perguntas associadas
        savedSection.setQuestions(questions); // Atualiza a lista na entidade em memória

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

        // Valida nome único para atualizações
        // Verifica se houve mudança no nome, idioma ou empresa para revalidar a unicidade
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
        existingSection.setDenyUse(dto.getDenyUse());
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
            // Impedir cadastro de perguntas em branco.
            if (!org.springframework.util.StringUtils.hasText(qDto.getLabel())) {
                throw new ValidationException("Question label cannot be blank.");
            }
            // Impedir cadastro de perguntas duplicadas no mesmo formulário.
            if (!distinctLabels.add(qDto.getLabel().trim().toLowerCase())) {
                throw new ValidationException("Duplicate question labels within the same form are not allowed: " + qDto.getLabel());
            }
        }
    }

    // Helper para converter entidade em DTO
    private SurveySectionDto convertToSurveySectionDto(SurveySection entity) {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDenyUse(entity.getDenyUse());
        dto.setLanguage(entity.getLanguage());
        // Garante que a company não é nula antes de acessar o ID
        dto.setCompanyId(entity.getCompany() != null ? entity.getCompany().getId() : null);
        dto.setActive(entity.getActive());
        dto.setQuestions(entity.getQuestions().stream()
                .map(this::convertToQuestionDto)
                .collect(Collectors.toList()));
        return dto;
    }

    // Helper para converter entidade em DTO
    private QuestionDto convertToQuestionDto(Question entity) {
        QuestionDto dto = new QuestionDto();
        dto.setId(entity.getId());
        dto.setLabel(entity.getLabel());
        dto.setType(entity.getType());
        dto.setMandatory(entity.getMandatory());
        dto.setOptions(entity.getOptions());
        return dto;
    }

    // Helper para converter DTO em entidade
    private Question convertToQuestionEntity(QuestionDto dto, SurveySection surveySection) {
        Question entity = new Question();
        // ID é opcional para novas perguntas, será gerado pelo JPA
        entity.setId(dto.getId()); // Permite que o ID seja setado para atualização de perguntas existentes
        entity.setSurveySection(surveySection);
        entity.setLabel(dto.getLabel());
        entity.setType(dto.getType());
        entity.setMandatory(dto.getMandatory());
        entity.setOptions(dto.getOptions());
        return entity;
    }
}
