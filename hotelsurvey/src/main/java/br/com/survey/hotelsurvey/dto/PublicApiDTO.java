package br.com.survey.hotelsurvey.dto;

import java.util.List;

public class PublicApiDTO {
    public record IQuestionDTO(long id, String label) {}

    public record IFormularioDTO(long id, String name, boolean denyUse, List<IQuestionDTO> questions) {}
}