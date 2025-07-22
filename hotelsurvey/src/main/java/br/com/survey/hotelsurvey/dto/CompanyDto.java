package br.com.survey.hotelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    @NotBlank(message = "Company name cannot be blank")
    private String name;
}