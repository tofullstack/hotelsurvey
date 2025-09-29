package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {

    @Query("SELECT DISTINCT s FROM SurveySection s LEFT JOIN FETCH s.questions q WHERE s.company.id = :companyId AND s.active = true")
    List<SurveySection> findByCompanyIdAndActiveTrueWithQuestions(@Param("companyId") Long companyId);


    //teste novo endpoint para formularios por serie empresa
    @Query("SELECT DISTINCT s FROM SurveySection s LEFT JOIN FETCH s.questions q WHERE s.company.serieEmpresa = :serieName")
    List<SurveySection> findByCompanySerieNameWithQuestions(String serieName);



    List<SurveySection> findByConditionalIsTrueAndCompanyId(Long companyId);



    //teste: novo filtro para tratamento no formadminservice SEM linguagem definida
    boolean existsByNameAndCompanyId(String name, Long companyId);

    @EntityGraph(attributePaths = {"questions"})
    Optional<SurveySection> findWithQuestionsById(Long id);

}