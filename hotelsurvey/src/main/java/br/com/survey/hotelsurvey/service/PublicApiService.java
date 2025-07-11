package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.PublicApiDTO.IFormularioDTO;
import br.com.survey.hotelsurvey.dto.PublicApiDTO.IQuestionDTO;
import br.com.survey.hotelsurvey.repository.SurveySectionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicApiService {

    private final SurveySectionRepository sectionRepository;

    public PublicApiService(SurveySectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<IFormularioDTO> getSurveyQuestions(Long companyId, String language) {
        return sectionRepository.findActiveByCompanyAndLanguageWithDetails(companyId, language)
                .stream()
                .map(section -> {
                    List<IQuestionDTO> questionDTOs = section.getQuestions().stream()
                            .map(question -> new IQuestionDTO(question.getId(), question.getLabel()))
                            .collect(Collectors.toList());

                    return new IFormularioDTO(
                            section.getId(),
                            section.getName(),
                            section.isDenyUse(),
                            questionDTOs
                    );
                })
                .collect(Collectors.toList());
    }
}