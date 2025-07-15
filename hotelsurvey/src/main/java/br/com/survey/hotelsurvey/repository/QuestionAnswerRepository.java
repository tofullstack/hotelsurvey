package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.QuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerRepository extends JpaRepository<QuestionAnswer, Long> {
}