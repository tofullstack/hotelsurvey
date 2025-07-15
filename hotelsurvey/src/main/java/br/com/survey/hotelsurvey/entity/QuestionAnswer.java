package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Entity
@Table(name = "Youtubes")
@NoArgsConstructor
@AllArgsConstructor
public class QuestionAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relaciona a resposta da pergunta à resposta geral do formulário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    // relaciona a resposta da pergunta à pergunta original do formulário
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // o ID da SurveySection à qual a pergunta pertence (para facilitar consultas analíticas)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_section_id", nullable = false)
    private SurveySection surveySection;

    // valor da resposta
    @Column(columnDefinition = "TEXT", nullable = false) // definido como TEXT para acomodar respostas longas
    private String answerValue;

    // indica se o hóspede informou que não utilizou o serviço (aplica-se a SurveySection.denyUse)
    @Column(nullable = false)
    private Boolean didNotUseService = false;
}