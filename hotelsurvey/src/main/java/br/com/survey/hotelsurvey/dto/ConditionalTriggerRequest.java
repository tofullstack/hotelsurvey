package br.com.survey.hotelsurvey.dto;

import lombok.Data;

@Data
public class ConditionalTriggerRequest {
    private Long questionId;
    private Long targetSectionId;
    private String triggerValue;
}