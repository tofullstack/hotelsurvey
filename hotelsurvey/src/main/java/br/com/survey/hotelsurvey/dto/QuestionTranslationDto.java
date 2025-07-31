package br.com.survey.hotelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionTranslationDto {
    @NotBlank(message = "Translation label cannot be blank")
    private String label;

    @NotBlank(message = "Translation language is required")
    private String language;
}

