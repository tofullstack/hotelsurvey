package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.ConditionalSectionTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConditionalSectionTriggerRepository extends JpaRepository<ConditionalSectionTrigger, Long> {



    List<ConditionalSectionTrigger> findByQuestionId(Long questionId);
}
