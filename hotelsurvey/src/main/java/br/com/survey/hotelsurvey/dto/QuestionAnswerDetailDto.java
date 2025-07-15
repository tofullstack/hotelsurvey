package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.entity.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswerDetailDto {
    private Long answerId; // ID da resposta da pergunta
    private Long questionId; // ID da pergunta original
    private String questionLabel; // Rótulo da pergunta original
    private QuestionType questionType; // Tipo da pergunta original
    private Long surveySectionId; // ID da seção
    private String surveySectionName; // Nome da seção
    private String answerValue; // O valor da resposta do hóspede
    private Boolean didNotUseService; // Se o hóspede marcou que não utilizou o serviço
}