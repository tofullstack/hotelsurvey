package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.entity.QuestionType;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
public class QuestionDto {
    private Long id;
    private Long surveySectionId;
    private QuestionType type;
    private String label;
    private Boolean deniable;
    private Boolean mandatory;
    private String displayType;
    private List<String> options;


    @NotEmpty(message = "At least one translation is required")
    private List<QuestionTranslationDto> translations;


    public QuestionDto(Long id, Long surveySectionId, QuestionType type, String label, Boolean deniable, Boolean mandatory, String displayType, List<String> options) {
        this.id = id;
        this.surveySectionId = surveySectionId;
        this.type = type;
        this.label = label;
        this.deniable = deniable;
        this.mandatory = mandatory;
        this.displayType = displayType;
        this.options = options;
    }
}