package br.com.survey.hotelsurvey.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "formularios", uniqueConstraints = {
        // REGRA: Nome da sessão único por empresa e idioma
        @UniqueConstraint(columnNames = {"nome", "id_empresa", "idioma"})
})
@Data
@NoArgsConstructor
public class Formulario {//represeta uma sessao de avaliacao
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String idioma; // Ex: "pt-BR", "en-US"

    private boolean ativo = true;

    private boolean denyUse; // O hóspede pode pular esta sessão

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    @OneToMany(mappedBy = "formulario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pergunta> perguntas;
}
