package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.ConditionalSectionTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConditionalSectionTriggerRepository extends JpaRepository<ConditionalSectionTrigger, Long> {


    // metodo para deletar todos os gatilhos associados a um formulário
    @Modifying
    @Query("DELETE FROM ConditionalSectionTrigger t WHERE t.question.surveySection.id = :surveySectionId")
    void deleteAllByQuestionSurveySectionId(@Param("surveySectionId") Long surveySectionId);

    List<ConditionalSectionTrigger> findByQuestionId(Long questionId);
}
