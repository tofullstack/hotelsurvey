package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString; // Importe ToString

import java.util.List;

@Data
@Entity
@Table(
        name = "survey_sections",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "company_id"})
)
@NoArgsConstructor
@AllArgsConstructor
public class SurveySection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String serieEmpresa;

    @Column(nullable = false)
    private Boolean conditional = false;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)

    private Company company;

    @OneToMany(mappedBy = "surveySection", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderBy("id ASC")
    @ToString.Exclude
    private List<Question> questions;
}