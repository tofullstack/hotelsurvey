package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    // método para deletar todas as perguntas de um formulário (SurveySection)
    @Modifying
    @Query("DELETE FROM Question q WHERE q.surveySection.id = :surveySectionId")
    void deleteAllBySurveySectionId(@Param("surveySectionId") Long surveySectionId);

//    @Query("SELECT q FROM Question q " +
//            "WHERE q.surveySection.company.serieEmpresa = :name")
//    List<Question> findByCompanySerieName(@Param("name") String name);

        @Query("SELECT q FROM Question q WHERE q.surveySection.serieEmpresa = :serieEmpresa")
        List<Question> findBySurveySectionSerie(@Param("serieEmpresa") String serieEmpresa);





}