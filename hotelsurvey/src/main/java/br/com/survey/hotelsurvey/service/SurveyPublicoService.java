package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.model.Formulario;
import br.com.survey.hotelsurvey.repository.FormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SurveyPublicoService {

    @Autowired
    private FormularioRepository formularioRepository;

    public List<Formulario> buscarFormulariosPublicos(Long empresaId, String idioma) {
        return formularioRepository.findPublicosByEmpresaAndIdioma(empresaId, idioma);
    }
}
