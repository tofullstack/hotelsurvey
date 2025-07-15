package br.com.survey.hotelsurvey.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerRequest {
    @NotNull(message = "Question ID cannot be null")
    private Long questionId;

    @NotNull(message = "Survey Section ID cannot be null")
    private Long surveySectionId;

    @NotBlank(message = "Answer value cannot be blank for questions that require a response")
    private String answerValue;

    // usado para perguntas do tipo 'denyUse' (se o hóspede não utilizou o serviço)
    private Boolean didNotUseService = false;
}