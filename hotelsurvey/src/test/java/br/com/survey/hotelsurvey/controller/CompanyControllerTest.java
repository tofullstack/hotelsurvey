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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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


    /* Cenario de Sucesso
     * Criando uma empresa com sucesso retorna 200 */
    @Test
    @DisplayName("Deve criar uma empresa com sucesso STATUS: 201")
    void shouldCreateCompanySuccessfully() throws Exception {
        CompanyDto requestDto = new CompanyDto();
        requestDto.setName("Empresa 1");
        requestDto.setSerieEmpresa("S001");

        CompanyDto responseDto = new CompanyDto();
        responseDto.setId(1L);
        responseDto.setName("Empresa 1");
        responseDto.setSerieEmpresa("S001");


        when(companyService.createCompany(any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Empresa 1"))
                .andExpect(jsonPath("$.serieEmpresa").value("S001"));
    }


    @Test
    @DisplayName("Deve retornar uma lista paginada com todas as empresas STATUS: 200")
    void shouldFindAllCompaniesSuccessfully() throws Exception {
        CompanyDto dto = new CompanyDto();
        dto.setName("Empresa 1");
        dto.setId(1L);
        dto.setSerieEmpresa("S001");

        Page<CompanyDto> page = new PageImpl<>(Arrays.asList(dto));

        when(companyService.getAllCompanies(any(String.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/companies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Empresa 1"))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }


    /* Cenario de Falha
     * Retorna 409 para criacao de empresas com nome duplicado
     * */
    @Test
    @DisplayName("Deve retornar DuplicateEntryException ao criar uma empresa com nome duplicado STATUS: 409")
    void shouldReturn409WhenDuplicateExceptinThrown() throws Exception {
        CompanyDto dto = new CompanyDto();
        dto.setName("Duplicado");
        dto.setSerieEmpresa("S001");
        dto.setId(1L);
        CompanyDto dto2 = new CompanyDto();
        dto2.setSerieEmpresa("S001");
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


    /* Cenario Falha
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

    /* Cenário Falha
     * Retorna uma lista vazia quando nenhuma empresa existe STATUS: 200 */
    @Test
    @DisplayName("Deve retornar uma página vazia quando nenhuma empresa existe STATUS: 200")
    void shouldReturnEmptyListWhenCompanyNotFound() throws Exception {
        Page<CompanyDto> emptyPage = Page.empty();

        when(companyService.getAllCompanies(any(String.class), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/companies")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /* Cenario de Falha
     * Retorna 404 ao criar uma empresa com dados nulos
     * */

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
     * Retorna 500 */
    @Test
    @DisplayName("Deve retornar InternalServerError STATUS: 500")
    void shoulReturnInternalServerErrorOnGetCompanies() throws Exception {
        when(companyService.getAllCompanies(any(String.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Oops"));

        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", containsString("An unexpected error occurred")));
    }
}