package br.com.survey.hotelsurvey.dto;


import lombok.Data;

@Data
public class JwtResponse {//dto para a resposta do login

    private String token;
    private String type = "Bearer";
    private Long id;
    private String login;
    private boolean precisaTrocarSenha;

    public JwtResponse(String accessToken, Long id, String login, boolean precisaTrocarSenha) {
        this.token = accessToken;
        this.id = id;
        this.login = login;
        this.precisaTrocarSenha = precisaTrocarSenha;
    }
}
