package br.com.survey.hotelsurvey.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name = "question_translations")
@NoArgsConstructor
@AllArgsConstructor
public class QuestionTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relacionamento com a pergunta original
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    
    @EqualsAndHashCode.Exclude // excluindo 'question' do equals/hashCode
    @ToString.Exclude
    private Question question;

    @Column(nullable = false)
    private String language; // ex: pt-BR, en-US

    @Column(nullable = false)
    private String label;
}