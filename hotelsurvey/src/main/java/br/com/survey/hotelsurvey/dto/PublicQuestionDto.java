package br.com.survey.hotelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicQuestionDto {
    private Long id;
    private String label;
    private String type;
    private Boolean required;
    private Boolean mandatory;
    private List<String> options;

}

//public class PublicQuestionDto {
//    private Long id;
//    private String label;
//}