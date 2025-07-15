package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.entity.UserProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreationRequest {
    @NotBlank(message = "Login cannot be blank")
    private String login;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotNull(message = "Profile cannot be null")
    private UserProfile profile;

    private Long companyId;
}
