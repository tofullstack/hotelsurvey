package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Entity
@Table(name = "survey_sections", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name", "language", "company_id"})
})
@NoArgsConstructor
@AllArgsConstructor
public class SurveySection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "deny_use", nullable = false)
    private Boolean denyUse = false;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "surveySection", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC") // Order questions for consistent output
    private List<Question> questions;
}
