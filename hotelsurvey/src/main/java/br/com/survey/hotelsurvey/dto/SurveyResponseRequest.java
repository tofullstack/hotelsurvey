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
public class SurveyResponseRequest {
    @NotNull(message = "Company ID cannot be null") //padronizando em ingles
    private Long companyId;

    private String language;

    private String guestIdentifier;

    @Valid
    @NotNull(message = "Answers list cannot be null")
    @Size(min = 1, message = "At least one answer is required")
    private List<QuestionAnswerRequest> answers;

    private String serieEmpresa;

    // campo para feedback livre, pode ser opcional
    private String freeTextFeedback;
}