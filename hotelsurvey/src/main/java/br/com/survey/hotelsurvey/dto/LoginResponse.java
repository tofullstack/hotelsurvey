package br.com.survey.hotelsurvey.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long userId;
    private String profile;
    private Boolean mustChangePassword;
}