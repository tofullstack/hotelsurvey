package br.com.survey.hotelsurvey.entity;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "questions")
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_section_id", nullable = false)
    private SurveySection surveySection;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private Boolean mandatory = false;

    // For CHOICE and SCALE types
    @Convert(converter = ListToJsonConverter.class)
    private List<String> options; // Store as comma-separated string or JSON

    private Boolean required;
}


