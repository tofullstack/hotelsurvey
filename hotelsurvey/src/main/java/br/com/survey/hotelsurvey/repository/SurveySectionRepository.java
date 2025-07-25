package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.entity.SurveySection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SurveySectionRepository extends JpaRepository<SurveySection, Long> {
    List<SurveySection> findByCompanyIdAndActiveTrue(Long companyId);
   // boolean existsByNameAndLanguageAndCompanyId(String name, String language, Long companyId);


    //teste: novo filtro para tratamento no formadminservice SEM linguagem definida
    boolean existsByNameAndCompanyId(String name, Long companyId);

    Optional<SurveySection> findByIdAndCompanyId(Long id, Long companyId);

    /* NOVO MÉTODO PARA API PÚBLICA  para tratamento do QRCODE no frontend*/
    Optional<SurveySection> findByIdAndCompanyIdAndActiveTrue(Long id, Long companyId);





}