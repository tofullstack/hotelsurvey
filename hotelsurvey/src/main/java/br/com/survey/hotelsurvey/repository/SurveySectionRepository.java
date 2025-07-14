package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {
    List<SurveySection> findByCompanyIdAndLanguageAndActiveTrue(Long companyId, String language);
    boolean existsByNameAndLanguageAndCompanyId(String name, String language, Long companyId);
    Optional<SurveySection> findByIdAndCompanyId(Long id, Long companyId);
}