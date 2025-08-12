package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.CompanyDto;
import br.com.survey.hotelsurvey.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@PreAuthorize("hasRole('ADMIN')") // apenas ADMIN pode gerenciar empresas
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<CompanyDto> createCompany(@Valid @RequestBody CompanyDto companyDto) {
        CompanyDto createdCompany = companyService.createCompany(companyDto);
        return new ResponseEntity<>(createdCompany, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<Page<CompanyDto>> getAllCompanies(
            @RequestParam(required = false) String name,
            Pageable pageable) {

        // Chama o service com os parâmetros de busca e paginação
        Page<CompanyDto> companiesPage = companyService.getAllCompanies(name, pageable);
        return ResponseEntity.ok(companiesPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompanyById(@PathVariable Long id) {
        CompanyDto company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDto> updateCompany(@PathVariable Long id, @Valid @RequestBody CompanyDto companyDto) {
        CompanyDto updatedCompany = companyService.updateCompany(id, companyDto);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }


}