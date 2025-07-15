package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
}