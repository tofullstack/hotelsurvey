package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.QuestionTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuestionTranslationRepository extends JpaRepository<QuestionTranslation, Long> {
    Optional<QuestionTranslation> findByQuestionIdAndLanguage(Long questionId, String language);
}