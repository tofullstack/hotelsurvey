package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import br.com.survey.hotelsurvey.entity.ConditionalSectionTrigger;
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
//@AllArgsConstructor
public class QuestionDto {
    private Long id;
    private Long surveySectionId;
    private QuestionType type;
    private String label;
    private Boolean deniable;
    private Boolean mandatory;
    private Boolean required;
    private List<String> options;

    @NotEmpty(message = "At least one translation is required")
    private List<QuestionTranslationDto> translations;




    public QuestionDto(Long id, Long surveySectionId, QuestionType type, String label, Boolean deniable, Boolean mandatory, Boolean required, List<String> options) {
        this.id = id;
        this.surveySectionId = surveySectionId;
        this.type = type;
        this.label = label;
        this.deniable = deniable;
        this.mandatory = mandatory;
        this.required = required;
        this.options = options;
    }
}
