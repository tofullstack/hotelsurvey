package br.com.survey.hotelsurvey.entity;

import br.com.survey.hotelsurvey.config.ListToJsonConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(name = "questions")
//@NoArgsConstructor
//@AllArgsConstructor
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



    @Convert(converter = ListToJsonConverter.class)
    private List<String> options;


    //teste: campo helper para formularios condicionais
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ConditionalSectionTrigger> triggers = new ArrayList<>();


    @Column(name = "question_order")
    private Integer orderIndex;

    // traduções da pergunta
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<QuestionTranslation> translations;

    public Question() {

        this.triggers = new ArrayList<>();
        this.translations = new HashSet<>();
    }

    public void addTranslation(QuestionTranslation translation) {
        this.translations.add(translation);
        translation.setQuestion(this);
    }


}