package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {



    // método para buscar uma lista de SurveyResponse pela string 'serieEmpresa'
    Optional<SurveyResponse> findBySerieEmpresa(String serieEmpresa);
}