package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.CompanyDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Company savedCompany = companyRepository.save(company);
        return convertToDto(savedCompany);
    }

    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
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
        return dto;
    }
}