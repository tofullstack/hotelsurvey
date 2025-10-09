package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "survey_responses")
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column
    private String serieEmpresa;

    @Column(nullable = false)
    private LocalDateTime responseDate;

    private String guestLastName;

    private String guestUH;


    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> answers;

    @Column(columnDefinition = "TEXT")
    private String freeTextFeedback;
}