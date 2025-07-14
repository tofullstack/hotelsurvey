package br.com.survey.hotelsurvey.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "empresas")
@Data // vai gerar getters, setters, toString, etc.
@NoArgsConstructor
public class Empresa {//representa o hotel
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;
}
