package br.com.survey.hotelsurvey.service;
import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.QuestionTranslationDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.QuestionTranslation;
import br.com.survey.hotelsurvey.entity.QuestionType;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.QuestionRepository;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import br.com.survey.hotelsurvey.service.FormAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormAdminService Tests")
public class FormAdminServiceTest {


    @Mock
    private SurveySectionRepository surveySectionRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private FormAdminService formAdminService;

    private Company testCompany;
    private SurveySection existingSurveySection;
    private SurveySectionDto validSurveySectionDto;
    private QuestionDto validQuestionDto;
    private QuestionTranslationDto ptBrTranslation;
    private QuestionTranslationDto enUsTranslation;

    @BeforeEach
    void setUp() {
        // inicializa dados de teste antes de cada método
        testCompany = new Company(1L, "Test Company");

        existingSurveySection = new SurveySection(1L, "Existing Form", false, true, testCompany, new ArrayList<>());


        ptBrTranslation = new QuestionTranslationDto("pt-BR", "Qual sua opinião?");
        enUsTranslation = new QuestionTranslationDto("en-US", "What's your opinion?");

        validQuestionDto = new QuestionDto();
        validQuestionDto.setType(QuestionType.TEXT);
        validQuestionDto.setMandatory(true);
        validQuestionDto.setDeniable(false);
        validQuestionDto.setRequired(true);
        validQuestionDto.setOptions(Collections.emptyList());
        validQuestionDto.setTranslations(Arrays.asList(ptBrTranslation, enUsTranslation));

        validSurveySectionDto = new SurveySectionDto();
        validSurveySectionDto.setName("Existing Form");
        validSurveySectionDto.setCompanyId(testCompany.getId());
        validSurveySectionDto.setActive(true);
        validSurveySectionDto.setQuestions(Collections.singletonList(validQuestionDto));


    }

    /* Testes para createForm @@@@
    *
    * Cenário de Sucesso @@
    * Criação de um Formulário com nome único e perguntas válidas @
    */
    @Test
    @DisplayName("Deve criar um novo formulario quando as perguntas forem validas e o nome unico")
    void shouldCreateFormSuccessfully() {
        // mock do comportamento do repositório
        when(surveySectionRepository.existsByNameAndCompanyId(anyString(), anyLong())).thenReturn(false);
        when(companyRepository.findById(testCompany.getId())).thenReturn(Optional.of(testCompany));
        when(surveySectionRepository.save(any(SurveySection.class))).thenReturn(existingSurveySection);
        when(questionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        // ação
        SurveySectionDto result = formAdminService.createForm(validSurveySectionDto);

        // verificações
        assertNotNull(result);
        assertEquals(validSurveySectionDto.getName(), result.getName());
        verify(surveySectionRepository, times(1)).existsByNameAndCompanyId(validSurveySectionDto.getName(), validSurveySectionDto.getCompanyId());
        verify(companyRepository, times(1)).findById(testCompany.getId());
        verify(surveySectionRepository, times(1)).save(any(SurveySection.class));
        verify(questionRepository, times(1)).saveAll(anyList());
    }

    /*Cenário de Falha @@
    * Nome de Formulário Duplicado @
    * */
    @Test
    @DisplayName("Deve retornar DuplicateEntryException quando o nome ja for existente para a mesma empresa na criacao do formulario")
    void shouldThrowDuplicateEntryExceptionWhenNameExists() {
        // mock para simular nome duplicado
        when(surveySectionRepository.existsByNameAndCompanyId(anyString(), anyLong())).thenReturn(true);

        // ação e Verificação da exceção
        DuplicateEntryException exception = assertThrows(DuplicateEntryException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("A form with this name and company already exists.", exception.getMessage());
        verify(surveySectionRepository, times(1)).existsByNameAndCompanyId(validSurveySectionDto.getName(), validSurveySectionDto.getCompanyId());
        verify(companyRepository, never()).findById(anyLong()); // não deve chamar o repositório da empresa
        verify(surveySectionRepository, never()).save(any(SurveySection.class)); // não deve salvar
    }

    /*Cenário de Falha @@
    * Empresa não encontrada @
    */

    @Test
    @DisplayName("Deve retornar ResourceNotFoundException quando a empresa nao e encontrada durante a criacao.")
    void shouldThrowResourceNotFoundExceptionWhenCompanyNotFound() {
        // mock para simular empresa não encontrada
        when(surveySectionRepository.existsByNameAndCompanyId(anyString(), anyLong())).thenReturn(false);
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("Company not found with ID: " + validSurveySectionDto.getCompanyId(), exception.getMessage());
        verify(companyRepository, times(1)).findById(testCompany.getId());
        verify(surveySectionRepository, never()).save(any(SurveySection.class));
    }

    /*Cenário de Falha @@
    * Lista vazia ou nula @
    * */
    @Test
    @DisplayName("Deve retornar ValidationException quando as questoes da lista sao nulas durante a criacao do formulario")
    void shouldThrowValidationExceptionWhenQuestionsListIsNull() {
        validSurveySectionDto.setQuestions(null); // define lista de perguntas como nula

        ValidationException exception = assertThrows(ValidationException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("A survey form must have at least one question.", exception.getMessage());
        verify(surveySectionRepository, never()).save(any(SurveySection.class));
    }

    @Test
    @DisplayName("Deve retornar ValidationException quando as questoes da lista sao vazias durante a criacao do formulario")
    void shouldThrowValidationExceptionWhenQuestionsListIsEmpty() {
        validSurveySectionDto.setQuestions(new ArrayList<>()); // define lista de perguntas como vazia

        ValidationException exception = assertThrows(ValidationException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("A survey form must have at least one question.", exception.getMessage());
        verify(surveySectionRepository, never()).save(any(SurveySection.class));
    }

    /* Cenário de Falha @@
    * Label de pergunta vazio/nulo na tradução principal @
    * */

    @Test
    @DisplayName("Deve retornar ValidationException quando o label da traducao da questao esta vazio durante a criacao do formulario")
    void shouldThrowValidationExceptionWhenQuestionLabelIsEmpty() {
        validQuestionDto.getTranslations().get(0).setLabel("");
        validSurveySectionDto.setQuestions(Collections.singletonList(validQuestionDto));
        validSurveySectionDto.setCompanyId(testCompany.getId());

        ValidationException exception = assertThrows(ValidationException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("Each question must have at least one non-empty translation label.", exception.getMessage());
        verify(surveySectionRepository, never()).save(any(SurveySection.class));
    }


    @Test
    @DisplayName("Deve retornar ValidationException quando o label de traducao da questao esta nulo durante a criacao do formulario.")
    void shouldThrowValidationExceptionWhenQuestionLabelIsNull() {
        validQuestionDto.getTranslations().get(0).setLabel(null); // tradução pt-BR nula
        validSurveySectionDto.setQuestions(Collections.singletonList(validQuestionDto));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        assertEquals("Each question must have at least one non-empty translation label.", exception.getMessage());
        verify(surveySectionRepository, never()).save(any(SurveySection.class));
    }

    /* Cenário de Falha @@
    * Perguntas com labels duplicados no mesmo formulário @
    * */

    @Test
    @DisplayName("Deve retornar ValidationException quando as labels das questoes forem duplicadas durante a criacao do formulario.")
    void shouldThrowValidationExceptionWhenDuplicateQuestionLabels() {
        //PROBLEMA IDENTIFICADO: service não fazia a validação do idioma e labels iguais

        QuestionDto duplicatedQuestionDto = new QuestionDto();
        duplicatedQuestionDto.setType(QuestionType.CHOICE); // pode ser qualquer tipo
        duplicatedQuestionDto.setMandatory(true);
        duplicatedQuestionDto.setDeniable(false);
        duplicatedQuestionDto.setRequired(true);
        duplicatedQuestionDto.setOptions(Collections.emptyList());
        duplicatedQuestionDto.setTranslations(Arrays.asList(
                new QuestionTranslationDto("Qual sua opiniao?", "pt-BR"),
                new QuestionTranslationDto("Qual sua opiniao?", "pt-BR")
        ));


        validSurveySectionDto.setQuestions(Collections.singletonList(duplicatedQuestionDto));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                formAdminService.createForm(validSurveySectionDto)
        );

        // o valor esperado deve ser a string exata que está sendo duplicada
        assertEquals("Duplicate translation within a question: Qual sua opiniao? [pt-BR]", exception.getMessage());


    }
}
