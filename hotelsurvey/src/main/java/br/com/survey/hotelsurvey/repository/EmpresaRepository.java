package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByNome(String nome);
}
