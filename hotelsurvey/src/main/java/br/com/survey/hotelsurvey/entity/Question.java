package br.com.survey.hotelsurvey.entity;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "questions")
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;


@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "survey_section_id", nullable = false)
@EqualsAndHashCode.Exclude // excluindo 'surveySection' do equals/hashCode
@ToString.Exclude     // excluindo 'surveySection' do toString
    private SurveySection surveySection;

    @Column(nullable = false)
    private Boolean deniable = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private Boolean mandatory = false;

    private Boolean required;

    @Convert(converter = ListToJsonConverter.class)
    private List<String> options;


    //teste: campo helper para formularios condicionais
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ConditionalSectionTrigger> triggers;



    // traduções da pergunta
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // excluindo'translations' do equals/hashCode
    @ToString.Exclude     // excluindo 'translations' do toString
    private Set<QuestionTranslation> translations;

    // helper opcional
    public QuestionTranslation getTranslationByLanguage(String language) {
        return translations.stream()
                .filter(t -> t.getLanguage().equalsIgnoreCase(language))
                .findFirst()
                .orElse(null);
    }
}