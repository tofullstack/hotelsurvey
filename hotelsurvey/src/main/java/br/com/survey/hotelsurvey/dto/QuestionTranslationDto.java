package br.com.survey.hotelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class QuestionTranslationDto {
    @NotBlank(message = "Translation label cannot be blank")
    private String label;

    @NotBlank(message = "Translation language is required")
    private String language;
}

