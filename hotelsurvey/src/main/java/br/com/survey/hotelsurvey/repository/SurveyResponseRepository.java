package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Passo 1: Importar

import java.util.List;
import java.util.Optional;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long>, JpaSpecificationExecutor<SurveyResponse> {

    Optional<SurveyResponse> findBySerieEmpresa(String serieEmpresa);

    List<SurveyResponse> findByCompanySerieEmpresaIgnoreCase(String serieEmpresa);

}