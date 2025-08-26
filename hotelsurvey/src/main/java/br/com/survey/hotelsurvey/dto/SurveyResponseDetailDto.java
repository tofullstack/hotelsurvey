package br.com.survey.hotelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponseDetailDto {
    private Long id;
    private String language;
    private Long companyId;
    private String companyName;
    private String serieEmpresa; //novo campo para inclusao da serie
    private LocalDateTime responseDate;
    private String guestIdentifier;
    private String freeTextFeedback;
    private List<QuestionAnswerDetailDto> answers;
}