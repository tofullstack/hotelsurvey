package br.com.survey.hotelsurvey.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDto {
    private long totalResponses;
    private Double averageRating;
    private Double ratingStandardDeviation;
//    private List<SurveyResponseDetailDto> responses;
}