package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import br.com.survey.hotelsurvey.entity.QuestionType;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private Long id;

    @NotBlank(message = "Question label cannot be blank")
    private String label;

    @NotNull(message = "Question type cannot be null")
    private QuestionType type;

    private Boolean mandatory = false;



    private List<String> options; // fpr CHOICE and SCALE types
}