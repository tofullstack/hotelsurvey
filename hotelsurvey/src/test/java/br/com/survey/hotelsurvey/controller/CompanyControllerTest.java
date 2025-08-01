package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.CompanyDto;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.security.JwtAuthenticationFilter;
import br.com.survey.hotelsurvey.security.JwtTokenProvider;
import br.com.survey.hotelsurvey.service.CompanyService;
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

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // import para tratamento da url


@WebMvcTest(controllers = CompanyController.class,
excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CompanyController Tests")
public class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;



    /* Cenário de Sucesso @@
    * Criando uma empresa com sucesso retorna 200 @*/
    @Test
    @DisplayName("Deve criar uma empresa com sucesso STATUS: 201")
    void shouldCreateCompanySuccessfully() throws Exception {
        CompanyDto requestDto = new CompanyDto();
        requestDto.setName("Empresa 1"); // NÃO envia o ID!

        CompanyDto responseDto = new CompanyDto();
        responseDto.setId(1L); // Esse ID é retornado pelo service após salvar no banco
        responseDto.setName("Empresa 1");

        when(companyService.createCompany(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                //.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Empresa 1"));
    }


    /* Cenário Sucesso @@
     * Retorna todas as empresas STATUS: 200 */
    @Test
    @DisplayName("Deve retornar uma lista com todas as empresas STATUS: 200")
    void shouldFindAllCompaniesSuccessfully() throws Exception {
        CompanyDto dto = new CompanyDto();
        dto.setName("Empresa 1");
        dto.setId(1L);

        when(companyService.getAllCompanies()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Empresa 1"));
    }

    /* Cenário de Falha @@
    * Retorna 409 para criação de empresas com nome duplicado
    * */
     @Test
     @DisplayName("Deve retornar DuplicateEntryException ao criar uma empresa com nome duplicado STATUS: 409")
     void shouldReturn409WhenDuplicateExceptinThrown() throws Exception {
        CompanyDto dto = new CompanyDto();
        dto.setName("Duplicado");
        dto.setId(1L);
        CompanyDto dto2 = new CompanyDto();
        dto2.setId(2L);
        dto2.setName("Duplicado");

        when(companyService.createCompany(any()))
                .thenThrow(new DuplicateEntryException("Company already exists"));

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Company already exists")));
     }


    /* Cenário Falha @@
    * Deve retornar uma empresa pelo ID STATUS: 404
    * */

    @Test
    @DisplayName("Deve retornar ResourceNotFound quando nao encontra uma empresa pelo ID STATUS: 404")
    void shouldReturn404WhenCompanyNotFound() throws Exception {
        when(companyService.getCompanyById(999L))
        .thenThrow(new ResourceNotFoundException("Company not found"));

        mockMvc.perform(get("/api/companies/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Company not found"));
    }

    /* Cenário Falha @@
    * Retorna uma lista vazia quando nenhuma empresa existe STATUS: 200
    */
    @Test
    @DisplayName("Deve retornar uma lista vazia quando nenhuma empresa existe STATUS: 200")
    void shouldReturnEmptyListWhenCompanyNotFound() throws Exception {
        when(companyService.getAllCompanies()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    /* Cenário de Falha
    * Retorna 404 ao criar uma empresa com dados nulos
    *  */

    @Test
    @DisplayName("Deve retornar BadRequest ao criar uma empresa com campos nulos STATUS: 404")
    void shoulReturnValidationExceptionOnCreateCompany() throws Exception {
        //dto com campos invalidos
        CompanyDto dto = new CompanyDto();

        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }

    /* Cenário de Falha
    * Retorna 500
     */
    @Test
    @DisplayName("Deve retornar InternalServerError STATUS: 500")
    void shoulReturnInternalServerErrorOnCreateCompany() throws Exception {
        when(companyService.getAllCompanies()).thenThrow(new RuntimeException("Oops"));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("An unexpected error occurred")));
    }
}
