package br.com.survey.hotelsurvey.repository;

import br.com.survey.hotelsurvey.model.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FormularioRepository extends JpaRepository<Formulario, Long> {

    // Para validar a regra de negócio de nome único
    boolean existsByNomeAndEmpresaIdAndIdioma(String nome, Long empresaId, String idioma);


    @Query("SELECT f FROM Formulario f LEFT JOIN FETCH f.perguntas p LEFT JOIN FETCH p.opcoes WHERE f.empresa.id = :empresaId AND f.idioma = :idioma AND f.ativo = true")
    List<Formulario> findPublicosByEmpresaAndIdioma(@Param("empresaId") Long empresaId, @Param("idioma") String idioma);

    @Query("SELECT f FROM Formulario f LEFT JOIN FETCH f.perguntas p LEFT JOIN FETCH p.opcoes WHERE f.id = :id")
    Optional<Formulario> findByIdWithDetails(@Param("id") Long id);


//    //  a API pública, buscando apenas os ativos
//    List<Formulario> findByEmpresaIdAndIdiomaAndAtivoTrue(Long empresaId, String idioma);
//
//    // buscar um formulário com todos os seus detalhes para edição (evita N+1)
//    @Query("SELECT f FROM Formulario f LEFT JOIN FETCH f.perguntas p LEFT JOIN FETCH p.opcoes WHERE f.id = :id")
//    Optional<Formulario> findByIdWithDetails(@Param("id") Long id);
}
