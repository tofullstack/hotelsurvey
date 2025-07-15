package br.com.survey.hotelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicSurveySectionDto {
    private Long id;
    private String name;
    private Boolean denyUse;
    private List<PublicQuestionDto> questions;
}
