package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.PublicQuestionDto;
import br.com.survey.hotelsurvey.dto.PublicSurveySectionDto;
import br.com.survey.hotelsurvey.entity.Question;
import br.com.survey.hotelsurvey.entity.QuestionTranslation;
import br.com.survey.hotelsurvey.entity.SurveySection;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
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
     * a API deve respeitar o idioma e o hotel passado como parâmetro.
     * a resposta deve trazer apenas formulários ativos e publicados.
     * a estrutura contempla os setores avaliáveis (chamados de "formulários") e suas respectivas perguntas.
     *
     * @param companyId Identificador único do hotel/estabelecimento.
     * @param language Idioma do formulário (ex: "pt-BR", "en-US").
     * @return Lista de PublicSurveySectionDto contendo a estrutura das perguntas.
     */
    public List<PublicSurveySectionDto> getSurveyQuestions(Long companyId, String language) {
        List<SurveySection> surveySections = surveySectionRepository.findByCompanyIdAndLanguageAndActiveTrue(companyId, language);

        return surveySections.stream()
                .map(section -> convertToPublicSurveySectionDto(section, language))
                .collect(Collectors.toList());
    }

    // helper para converter entidade em DTO público
    private PublicSurveySectionDto convertToPublicSurveySectionDto(SurveySection entity, String language) {
        PublicSurveySectionDto dto = new PublicSurveySectionDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());

        dto.setQuestions(entity.getQuestions().stream()
                .map(question -> toPublicDto(question, language))
                .collect(Collectors.toList())
        );

        return dto;
    }

    // NOVO MÉTODO PARA API PÚBLICA
    public PublicSurveySectionDto getSingleSurveySection(Long companyId, String language, Long sectionId) {
        SurveySection section = surveySectionRepository.findByIdAndCompanyIdAndLanguageAndActiveTrue(sectionId, companyId, language)
                .orElseThrow(() -> new ResourceNotFoundException("Survey section with ID " + sectionId + " not found or not active for given company and language."));
        return convertToPublicSurveySectionDto(section, language);
    }


    public PublicQuestionDto toPublicDto(Question question, String language) {
        PublicQuestionDto dto = new PublicQuestionDto();
        dto.setId(question.getId());
        dto.setType(question.getType().name());
        dto.setMandatory(question.getMandatory());
        dto.setRequired(question.getRequired());
        dto.setOptions(question.getOptions());

        String label = question.getTranslations().stream()
                .filter(t -> t.getLanguage().equalsIgnoreCase(language))
                .map(QuestionTranslation::getLabel)
                .findFirst()
                .orElse("Tradução indisponível");

        dto.setLabel(label);
        return dto;
    }



}
