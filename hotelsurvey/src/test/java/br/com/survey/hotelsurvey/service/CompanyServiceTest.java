package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.CompanyDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyService Tests")
public class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    private Company testCompany;
    private CompanyDto testCompanyDto;

    @BeforeEach
    void setUp() {
        testCompany = new Company(1L, "Empresa Teste", "S123");
        testCompanyDto = new CompanyDto(1L, "Empresa Teste", "S123");
    }

    /* Cenário Sucesso @@
      Criar uma empresa
    */
    @Test
    @DisplayName("Deve criar uma nova empresa com sucesso")
    void shouldCreateCompanySuccessfully() {
        when(companyRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        CompanyDto result = companyService.createCompany(testCompanyDto);

        assertNotNull(result);
        assertEquals(testCompanyDto.getName(), result.getName());
        verify(companyRepository, times(1)).findByName(testCompanyDto.getName());
        verify(companyRepository, times(1)).save(any(Company.class));
    }


    /* Cenário de Falha @@
     * Criando uma empresa com nome duplicado
     */
    @Test
    @DisplayName("Deve lancar DuplicateEntryException ao criar uma empresa com nome duplicado")
    void shouldThrowDuplicateEntryExceptionWhenCreatingCompanyWithDuplicateName() {
        when(companyRepository.findByName(anyString())).thenReturn(Optional.of(testCompany));

        DuplicateEntryException exception = assertThrows(DuplicateEntryException.class, () ->
                companyService.createCompany(testCompanyDto)
        );

        assertEquals("Company with name '" + testCompanyDto.getName() + "' already exists.", exception.getMessage());
        verify(companyRepository, times(1)).findByName(testCompanyDto.getName());
        verify(companyRepository, never()).save(any(Company.class));
    }

    /* Cenário de Sucesso @@
     * Resgatando uma lista de empresas paginada
     */
    @Test
    @DisplayName("Deve retornar uma página de empresas quando o filtro é nulo")
    void shouldReturnAllCompaniesWithPaginationWhenNameIsNull() {
        // Mocka a lista de empresas retornada pelo repositório
        List<Company> companies = Arrays.asList(testCompany, new Company(2L, "Outra Empresa Teste", "S321"));
        Page<Company> companyPage = new PageImpl<>(companies);

        // Mocka a chamada ao repositório para o método paginado
        when(companyRepository.findAll(any(Pageable.class))).thenReturn(companyPage);

        // Chama o service com nome nulo para simular a busca de todas as empresas
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyDto> result = companyService.getAllCompanies(null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Empresa Teste", result.getContent().get(0).getName());
        verify(companyRepository, times(1)).findAll(pageable);
        verify(companyRepository, never()).findByNameContainingIgnoreCase(any(), any());
    }

    /* Cenário de Sucesso @@
     * Buscando empresas por nome com paginação
     */
    @Test
    @DisplayName("Deve retornar uma página de empresas quando um nome é fornecido")
    void shouldReturnFilteredCompaniesWithPaginationWhenNameIsProvided() {
        // Mocka a lista de empresas que correspondem ao filtro
        List<Company> filteredCompanies = Collections.singletonList(testCompany);
        Page<Company> filteredPage = new PageImpl<>(filteredCompanies);

        // Mocka a chamada ao repositório para o método de busca paginado
        when(companyRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class))).thenReturn(filteredPage);

        // Chama o service com um nome para simular a busca
        Pageable pageable = PageRequest.of(0, 10);
        Page<CompanyDto> result = companyService.getAllCompanies("Empresa Teste", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Empresa Teste", result.getContent().get(0).getName());
        verify(companyRepository, times(1)).findByNameContainingIgnoreCase("Empresa Teste", pageable);
        verify(companyRepository, never()).findAll(any(Pageable.class));
    }

    /* Cenário de Sucesso @@
     * Buscar uma empresa pelo ID @
     */

    @Test
    @DisplayName("Deve retornar a empresa correta quando o ID for encontrado")
    void shouldReturnCompanyByIdSuccessfully() {
        when(companyRepository.findById(anyLong())).thenReturn(Optional.of(testCompany));
        CompanyDto result = companyService.getCompanyById(1L);

        assertNotNull(result);
        assertEquals(testCompany.getName(), result.getName());
        verify(companyRepository, times(1)).findById(1L);
    }

    /* Cenário de Falha @@
     * Buscar uma empresa não existente @
     */
    @Test
    @DisplayName("Deve lancar ResourceNotFoundException quando o ID da empresa nao for encontrado")
    void shouldThrowResourceNotFoundExceptionWhenCompanyNotFoundById() {
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                companyService.getCompanyById(99L)
        );

        assertEquals("Company not found with ID: 99", exception.getMessage());
        verify(companyRepository, times(1)).findById(99L);
    }

    /* Cenário de Sucesso @@
     * Atualizando uma empresa com sucesso @
     */
    @Test
    @DisplayName("Deve atualizar uma empresa com sucesso quando o nome nao for duplicado")
    void shouldUpdateCompanySuccessfully() {
        CompanyDto updatedDto = new CompanyDto(1L, "Updated Company Name", "S123");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.findByName("Updated Company Name")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(new Company(1L, "Updated Company Name", "S123"));

        CompanyDto result = companyService.updateCompany(1L, updatedDto);

        assertNotNull(result);
        assertEquals("Updated Company Name", result.getName());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).findByName("Updated Company Name");
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    /* Cenário de Falha @@
     * Tentando atualizar uma empresa inexistente @
     */

    @Test
    @DisplayName("Deve lancar ResourceNotFoundException ao tentar atualizar uma empresa inexistente")
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonexistentCompany() {
        CompanyDto updatedDto = new CompanyDto(99L, "Nonexistent Company", "S999");
        when(companyRepository.findById(anyLong())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                companyService.updateCompany(99L, updatedDto)
        );

        assertEquals("Company not found with ID: 99", exception.getMessage());
        verify(companyRepository, times(1)).findById(99L);
        verify(companyRepository, never()).save(any(Company.class));
    }

    /* Cenário de Falha @@
     * Atualizar uma empresa com nome duplicado @
     */

    @Test
    @DisplayName("Deve lancar DuplicateEntryException ao atualizar uma empresa com nome que ja existe para outra")
    void shouldThrowDuplicateEntryExceptionWhenUpdatingWithDuplicateName() {
        Company existingCompany2 = new Company(2L, "Existing Company 2", "S321");
        CompanyDto updatedDto = new CompanyDto(1L, "Existing Company 2", "S321");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.findByName("Existing Company 2")).thenReturn(Optional.of(existingCompany2));

        DuplicateEntryException exception = assertThrows(DuplicateEntryException.class, () ->
                companyService.updateCompany(1L, updatedDto)
        );

        assertEquals("Company with name 'Existing Company 2' already exists.", exception.getMessage());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).findByName("Existing Company 2");
        verify(companyRepository, never()).save(any(Company.class));
    }

    /* Cenário de Sucesso @@
     * Atualizando uma empresa com sucesso @
     */
    @Test
    @DisplayName("Deve atualizar uma empresa com sucesso mesmo se o nome for o mesmo")
    void shouldUpdateCompanySuccessfullyWithSameName() {
        CompanyDto sameNameDto = new CompanyDto(1L, "Empresa Teste", "S123");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        CompanyDto result = companyService.updateCompany(1L, sameNameDto);

        assertNotNull(result);
        assertEquals("Empresa Teste", result.getName());
        verify(companyRepository, times(1)).findById(1L);
        verify(companyRepository, times(1)).save(any(Company.class));
        verify(companyRepository, never()).findByName(anyString());
    }

    /* Cenário de Sucesso @@
     * Deletar empresa com sucesso @
     */
    @Test
    @DisplayName("Deve chamar o metodo deleteById do repositorio para excluir a empresa")
    void shouldDeleteCompanyById() {
        companyService.deleteCompany(1L);

        verify(companyRepository, times(1)).deleteById(1L);
    }
}