package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {
//    List<SurveySection> findByCompanyIdAndActiveTrue(Long companyId);

    @Query("SELECT DISTINCT s FROM SurveySection s LEFT JOIN FETCH s.questions q WHERE s.company.id = :companyId AND s.active = true")
    List<SurveySection> findByCompanyIdAndActiveTrueWithQuestions(@Param("companyId") Long companyId);

//    @Query("SELECT s FROM SurveySection s JOIN FETCH s.questions q JOIN FETCH q.translations t WHERE s.company.id = :companyId AND s.active = true")
//    List<SurveySection> findByCompanyIdAndActiveTrueWithQuestionsAndTranslations(@Param("companyId") Long companyId);

    List<SurveySection> findByConditionalIsTrueAndCompanyId(Long companyId);


    //teste: novo filtro para tratamento no formadminservice SEM linguagem definida
    boolean existsByNameAndCompanyId(String name, Long companyId);

    //forçar o carregamento das perguntas
    @EntityGraph(attributePaths = {"questions"})
    Optional<SurveySection> findWithQuestionsById(Long id);
//
//    /* NOVO MÉTODO PARA API PÚBLICA  para tratamento do QRCODE no frontend*/
//    Optional<SurveySection> findByIdAndCompanyIdAndActiveTrue(Long id, Long companyId);
//
//    @EntityGraph(attributePaths = {"questions"})
//    Optional<SurveySection> findByIdAndActiveTrue(Long id);
}