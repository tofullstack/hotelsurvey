package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {

    @Query("SELECT s FROM SurveySection s LEFT JOIN FETCH s.questions WHERE s.companyId = :companyId AND s.language = :language AND s.active = true")
    List<SurveySection> findActiveByCompanyAndLanguageWithDetails(Long companyId, String language);
}