package br.com.survey.hotelsurvey.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "opcoes_pergunta")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id") //evita problemas comuns com JPA ao comparar entidades
public class Opcao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O texto da opção não pode ser vazio.")
    @Column(name = "texto_opcao", nullable = false)
    private String texto; // EXEMPLO: "Ótimo", "Bom", "Regular"


    /**
     * Valor numérico associado à opção.
     * Essencial para cálculos de média, score, NPS, etc.
     * Ex: "Excelente" pode ter valor 5, "Ruim" pode ter valor 1.
     */
    @Column(name = "valor_opcao", nullable = false)
    private int valor;

    @ManyToOne(fetch = FetchType.LAZY) //cada opcao pertence a uma unica pergunta, lazy pra nao comparar a pergunta inteira
    @JoinColumn(name = "id_pergunta", nullable = false)
    @JsonIgnore
    private Pergunta pergunta;

    /**
     * Construtor completo para facilitar a criação de novas opções.
     * @param texto Descrição da opção (o que o usuário vê).
     * @param valor O peso numérico da opção para análise.
     * @param pergunta A entidade Pergunta à qual esta opção pertence.
     */

    public Opcao(String texto, int valor, Pergunta pergunta) {
        this.texto = texto;
        this.valor = valor;
        this.pergunta = pergunta;
    }
}