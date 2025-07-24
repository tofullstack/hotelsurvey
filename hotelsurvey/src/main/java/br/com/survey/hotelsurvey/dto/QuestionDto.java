package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import br.com.survey.hotelsurvey.entity.QuestionType;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private String type;
    private boolean mandatory;
    private List<String> options;

    @NotEmpty(message = "At least one translation is required")
    private List<QuestionTranslationDto> translations;


}
