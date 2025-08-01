package br.com.survey.hotelsurvey.controller;


import br.com.survey.hotelsurvey.dto.QuestionDto;
import br.com.survey.hotelsurvey.dto.QuestionTranslationDto;
import br.com.survey.hotelsurvey.dto.SurveySectionDto;
import br.com.survey.hotelsurvey.entity.QuestionType;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.security.JwtAuthenticationFilter;
import br.com.survey.hotelsurvey.security.JwtTokenProvider;

import br.com.survey.hotelsurvey.service.FormAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;





@WebMvcTest(
        controllers = FormAdminController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("FormAdminController Tests")
public class FormAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FormAdminService formAdminService; //nao se usa mais mockbean? TODO: ler a doc da v.3.2 do springboot


    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    /* Cenário de Teste Sucesso @@
    * Criando um formulário com sucesso retorna 200 @
    * */

    @Test
    @DisplayName("Deve criar um novo formulario com sucesso STATUS: 201")
    void shouldCreateFormSuccessfully() throws Exception {
        SurveySectionDto requestDto = new SurveySectionDto();
        requestDto.setName("Academia");
        requestDto.setCompanyId(1L);

        SurveySectionDto responseDto = new SurveySectionDto();
        responseDto.setId(1L);
        responseDto.setName("Academia");

        when(formAdminService.createForm(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                //.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Academia"));
    }

    /*Cenário Sucesso @@
    * Retorna todos os formularios com status 200 @*/

    @Test
    @DisplayName("Deve retornar uma lista com todos os formularios STATUS: 200")
    void shouldReturnAllForms() throws Exception {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(1L);
        dto.setName("Academia");

        when(formAdminService.getAllForms()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Academia"));
    }

    /* Cenário de Sucesso @@
    * 200 Retorna uma lista com os formulários filtrados pelo STATUS e EMPRESA */

    @Test
    @DisplayName("Deve retornar uma lista de formularios filtrados pelo STATUS e EMPRESA  STATUS 200")
    void shouldSearchFormsByCompanyAndStatus() throws Exception {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(1L);
        dto.setName("Academia");

        when(formAdminService.searchForms("hotel", "ativos")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/forms/search")
                        .param("companyName", "hotel")
                        .param("status", "ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Academia"));
    }

    /* Cenário de Sucesso @@
    * Retorna 200 com o formulário traduzido @*/
    @Test
    @DisplayName("Deve traduzir o formulario com sucesso STATUS: 200")
    void shouldReturnTranslatedFormSuccessfully() throws Exception {
        SurveySectionDto dto = new SurveySectionDto();
        dto.setId(10L);
        dto.setName("Translated");

        when(formAdminService.getFormWithTranslatedQuestions(10L, "en-US")).thenReturn(dto);

        mockMvc.perform(get("/api/forms/questions/10/language/en-US"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Translated"));
    }



    /* Cenário de Falha @@
    * Criando um formulário com campos obrigatórios faltantes retorna 400 @
    * */

    @Test
    @DisplayName("Deve retornar BadRequest ao criar um formulario com erros de validacao STATUS: 400")
    void shouldReturn400WhenValidationFails() throws Exception {
        SurveySectionDto invalidDto = new SurveySectionDto(); // falta campo obrigatório

        mockMvc.perform(post("/api/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    /* Cenário de Falha @@
    * Retorna 409 para criação de formulário duplicado @ */


    @Test
    @DisplayName("Deve retornar DuplicateEntryException ao criar um formulario duplicado STATUS: 409")
    void shouldReturn409WhenDuplicateExceptionThrown() throws Exception {
        QuestionTranslationDto translation = new QuestionTranslationDto("Texto da Pergunta", "pt-BR");

        //objeto de pergunta válido
        QuestionDto question = new QuestionDto();
        question.setTranslations(List.of(translation));
        question.setType(QuestionType.TEXT);
        question.setLabel("Título da Pergunta");

        SurveySectionDto dto = new SurveySectionDto();
        dto.setName("Duplicado");
        dto.setCompanyId(1L);
        dto.setQuestions(List.of(question));

        when(formAdminService.createForm(any()))
                .thenThrow(new DuplicateEntryException("Form already exists"));

        mockMvc.perform(post("/api/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Form already exists"));
    }

    /* Cenário Falha @@
    * Formulário não encontrado retorna 404 @
    * */

    @Test
    @DisplayName("Deve retornar ResourceNotFound quando nao encontra o formulario pelo ID STATUS: 404")
    void shouldReturn404WhenFormNotFound() throws Exception {
        when(formAdminService.getFormById(999L))
                .thenThrow(new ResourceNotFoundException("Form not found"));

        mockMvc.perform(get("/api/forms/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Form not found"));
    }

    /* Cenário Falha @@
    * Retorna uma lista vazia quando nenhum formulário existe @*/
    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhum formulario existe STATUS: 200")
    void shouldReturnEmptyListWhenNoFormsExist() throws Exception {
        when(formAdminService.getAllForms()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/forms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /* Cenário Falha @@
    * Retorna 500*/

    @Test
    @DisplayName("Deve retornar InternalServerError STATUS: 500")
    void shouldHandleUnexpectedError() throws Exception {
        when(formAdminService.getAllForms()).thenThrow(new RuntimeException("Oops"));

        mockMvc.perform(get("/api/forms"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("An unexpected error occurred")));
    }


    /* Cenário de Falha @@
    * Retorna 404 ao criar um formulário @*/
    @Test
    @DisplayName("Deve retornar BadRequest ao criar um formulario com campos nulos STATUS: 404")
    void shouldReturnValidationExceptionOnCreate() throws Exception {
        // DTO com campos inválidos
        SurveySectionDto dto = new SurveySectionDto();
        dto.setName("Academia"); // companyId e questions serão nulos

        mockMvc.perform(post("/api/forms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }


}
