package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.CompanyDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public CompanyDto createCompany(CompanyDto companyDto) {
        if (companyRepository.findByName(companyDto.getName()).isPresent()) {
            throw new DuplicateEntryException("Company with name '" + companyDto.getName() + "' already exists.");
        }
        Company company = new Company();
        company.setName(companyDto.getName());
        company.setSerieEmpresa(companyDto.getSerieEmpresa());
        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }

    // Este é o método correto e único para buscar empresas
    public Page<CompanyDto> getAllCompanies(String name, Pageable pageable) {
        Page<Company> companiesPage;

        // Lógica para decidir se a busca é com filtro ou sem
        if (StringUtils.hasText(name)) {
            companiesPage = companyRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            companiesPage = companyRepository.findAll(pageable);
        }

        // Converte a Page de Company para uma Page de CompanyDto
        return companiesPage.map(this::convertToDto);
    }


    public CompanyDto getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        return convertToDto(company);
    }

    @Transactional
    public CompanyDto updateCompany(Long id, CompanyDto companyDto) {
        Company existingCompany = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));

        if (!existingCompany.getName().equals(companyDto.getName()) && companyRepository.findByName(companyDto.getName()).isPresent()) {
            throw new DuplicateEntryException("Company with name '" + companyDto.getName() + "' already exists.");
        }

        existingCompany.setName(companyDto.getName());
        existingCompany.setSerieEmpresa(companyDto.getSerieEmpresa()); // Adicionado
        Company updatedCompany = companyRepository.save(existingCompany);
        return convertToDto(updatedCompany);
    }

    // soft delete
    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }

    private CompanyDto convertToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setSerieEmpresa(company.getSerieEmpresa());
        return dto;
    }
}