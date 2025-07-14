package br.com.survey.hotelsurvey.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String login;
    private String senha;
}
