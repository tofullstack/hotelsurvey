package br.com.survey.hotelsurvey.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perguntas")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id") // evita problemas de performance e recursividade com o @Data
@ToString(exclude = {"formulario", "opcoes"}) // evita recursão e LazyInitializationException
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O texto da pergunta não pode ser vazio.") //REGRA
    @Column(nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_formulario", nullable = false)
    @JsonIgnore
    private Formulario formulario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPergunta tipo;

    @Column(nullable = false)
    private boolean obrigatoria = false; //definindo valor padrao

    // relacionamento para opcoes
    @OneToMany(
            mappedBy = "pergunta",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER // Justificado abaixo
    )
    private List<Opcao> opcoes = new ArrayList<>();

    public enum TipoPergunta {
        ESCRITA,    // nao usará a lista de opcoes
        ESCOLHA,    // uará a lista de opções
        FECHADA,    // não usará a lista de opções (as opções "Sim/Não" são implícitas)
        ESCALA      // usará a lista de opções (ex: "1", "2", "3", "4", "5")
    }

    // --- Métodos de conveniência (Helpers) ---

    /**
     * Adiciona uma opção a esta pergunta e estabelece uma relação bidirecional.
     * É útil para construir a pergunta e suas opções de forma fluente.
     * @param textoOpcao O texto da opção a ser adicionada (ex: "Satisfatório").
     */


    public void adicionarOpcao(String textoOpcao, int valorOpcao) {
        if (this.tipo != TipoPergunta.ESCOLHA && this.tipo != TipoPergunta.ESCALA) {
            throw new UnsupportedOperationException("Não é possível adicionar opções para perguntas do tipo " + this.tipo);
        }
        // Usa o construtor completo da classe Opcao
        Opcao novaOpcao = new Opcao(textoOpcao, valorOpcao, this);
        this.opcoes.add(novaOpcao);
    }

//    public void adicionarOpcao(String textoOpcao) {
//
//        // validação para garantir que apenas perguntas relevantes tenham opções
//        if (this.tipo != TipoPergunta.ESCOLHA && this.tipo != TipoPergunta.ESCALA) {
//            throw new UnsupportedOperationException("Não é possível adicionar opções para perguntas do tipo " + this.tipo);
//        }
//        Opcao novaOpcao = new Opcao(textoOpcao, this);
//        this.opcoes.add(novaOpcao);
//    }
}