package br.com.survey.hotelsurvey.dto;

import br.com.survey.hotelsurvey.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String login;
    private Boolean active;
    private UserProfile profile;
    private Long companyId;
    private String companyName;
}