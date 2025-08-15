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

    // relaciona a resposta a uma empresa específica
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column
    private String serieEmpresa;

    // data e hora em que a resposta foi registrada
    @Column(nullable = false)
    private LocalDateTime responseDate;

    // identificador único do hóspede (pode ser o ID da reserva, email, etc. - para este exemplo, um String simples)
    private String guestIdentifier;

    // lista de respostas individuais a cada pergunta
    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionAnswer> answers;

    // aampo para feedback livre (pode ser adicionado ao final do formulário)
    @Column(columnDefinition = "TEXT")
    private String freeTextFeedback;
}