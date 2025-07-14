package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.PublicQuestionDto;
import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicApiService {

    @Autowired
    private SurveySectionRepository surveySectionRepository;

    /**
     * Retorna a estrutura das perguntas de satisfação para uma empresa e idioma específicos.
     * A API deve respeitar o idioma e o hotel passado como parâmetro.
     * A resposta deve trazer apenas formulários ativos e publicados.
     * A estrutura contempla os setores avaliáveis (chamados de "formulários") e suas respectivas perguntas.
     *
     * @param companyId Identificador único do hotel/estabelecimento.
     * @param language Idioma do formulário (ex: "pt-BR", "en-US").
     * @return Lista de PublicSurveySectionDto contendo a estrutura das perguntas.
     */
    public List<PublicSurveySectionDto> getSurveyQuestions(Long companyId, String language) {
        // Retorna apenas formulários ativos para a empresa e idioma especificados
        List<SurveySection> surveySections = surveySectionRepository.findByCompanyIdAndLanguageAndActiveTrue(companyId, language);

        return surveySections.stream()
                .map(this::convertToPublicSurveySectionDto)
                .collect(Collectors.toList());
    }

    // Helper para converter entidade em DTO público
    private PublicSurveySectionDto convertToPublicSurveySectionDto(SurveySection entity) {
        PublicSurveySectionDto dto = new PublicSurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDenyUse(entity.getDenyUse()); // O campo denyUse permite ao hóspede informar que não utilizou aquele serviço.
        dto.setQuestions(entity.getQuestions().stream()
                .map(question -> new PublicQuestionDto(question.getId(), question.getLabel()))
                .collect(Collectors.toList()));
        return dto;
    }
}
