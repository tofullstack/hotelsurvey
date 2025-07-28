package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "conditional_section_triggers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConditionalSectionTrigger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // pergunta que será usada como gatilho
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private Question question;

    // seção que será mostrada caso a condição seja verdadeira
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_section_id", nullable = false)
    @ToString.Exclude
    private SurveySection targetSection;

    // valor ou faixa de valores que disparam essa condição (ex: "1,2,3" ou "false")
    @Column(nullable = false)
    private String triggerValue;
}

