package br.com.survey.hotelsurvey.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveySectionDto {
    private Long id;

    @NotBlank(message = "Section name cannot be blank")
    private String name;

    @NotNull(message = "Company ID cannot be null")
    private Long companyId;


    private String companyName;

    private String language;

    @NotNull(message = "Serie cannot be null")
    private String serieEmpresa;

    private Boolean conditional;

    private Boolean active = true;

    @Valid
    @Size(min = 1, message = "A survey section must have at least one question")
    private List<QuestionDto> questions;

    private List<ConditionalTriggerRequest> triggers;


}