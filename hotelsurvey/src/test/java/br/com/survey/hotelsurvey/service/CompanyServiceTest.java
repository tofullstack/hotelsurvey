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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        testCompany = new Company(1L, "Empresa Teste");
        testCompanyDto = new CompanyDto(1L, "Empresa Teste");
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
        // uma empresa com o mesmo nome já existe.
        when(companyRepository.findByName(anyString())).thenReturn(Optional.of(testCompany));

        DuplicateEntryException exception = assertThrows(DuplicateEntryException.class, () ->
                companyService.createCompany(testCompanyDto)
        );

        assertEquals("Company with name '" + testCompanyDto.getName() + "' already exists.", exception.getMessage());
        verify(companyRepository, times(1)).findByName(testCompanyDto.getName());
        verify(companyRepository, never()).save(any(Company.class));
    }

    /* Cenário de Sucesso @@
     * Resgatando uma lista de empresas
     */
    @Test
    @DisplayName("Deve retornar uma lista de todas as empresas")
    void shouldReturnAllCompanies() {
        List<Company> companies = Arrays.asList(testCompany, new Company(2L, "Outra Empresa Teste"));
        when(companyRepository.findAll()).thenReturn(companies);

        List<CompanyDto> result = companyService.getAllCompanies();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Empresa Teste", result.get(0).getName());
        assertEquals("Outra Empresa Teste", result.get(1).getName());
        verify(companyRepository, times(1)).findAll();
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
        // nenhuma empresa é encontrada com o ID fornecido.
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
        // a empresa a ser atualizada existe e o novo nome não é duplicado.
        CompanyDto updatedDto = new CompanyDto(1L, "Updated Company Name");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.findByName("Updated Company Name")).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(new Company(1L, "Updated Company Name"));

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
        // a empresa a ser atualizada não existe.
        CompanyDto updatedDto = new CompanyDto(99L, "Nonexistent Company");
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
        Company existingCompany2 = new Company(2L, "Existing Company 2");
        CompanyDto updatedDto = new CompanyDto(1L, "Existing Company 2");
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
        // a empresa a ser atualizada existe e o nome não é alterado.
        CompanyDto sameNameDto = new CompanyDto(1L, "Empresa Teste");
        when(companyRepository.findById(1L)).thenReturn(Optional.of(testCompany));
        when(companyRepository.save(any(Company.class))).thenReturn(testCompany);

        CompanyDto result = companyService.updateCompany(1L, sameNameDto);

        // o nome do DTO retornado deve ser o mesmo
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