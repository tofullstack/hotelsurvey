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
import org.springframework.util.StringUtils;
import org.springframework.data.domain.Pageable;

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

    public Page<CompanyDto> getAllCompanies(String name, String status, Pageable pageable) {
        Page<Company> companiesPage;

        if ("inactive".equalsIgnoreCase(status)) {
            companiesPage = StringUtils.hasText(name)
                    ? companyRepository.findByNameContainingIgnoreCaseAndActiveFalse(name, pageable)
                    : companyRepository.findByActiveFalse(pageable);
        } else if ("all".equalsIgnoreCase(status)) {
            companiesPage = StringUtils.hasText(name)
                    ? companyRepository.findByNameContainingIgnoreCase(name, pageable)
                    : companyRepository.findAll(pageable);
        } else { // ativo por padrão
            companiesPage = StringUtils.hasText(name)
                    ? companyRepository.findByNameContainingIgnoreCaseAndActiveTrue(name, pageable)
                    : companyRepository.findByActiveTrue(pageable);
        }

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
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + id));
        company.setActive(false);
        companyRepository.save(company);
    }

    public void setCompanyActiveStatus(Long companyId, boolean active) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        company.setActive(active);
        companyRepository.save(company);
    }


    private CompanyDto convertToDto(Company company) {
        CompanyDto dto = new CompanyDto();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setSerieEmpresa(company.getSerieEmpresa());
        dto.setActive(company.getActive());
        return dto;
    }
}