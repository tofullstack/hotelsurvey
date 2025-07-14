package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.model.Formulario;
import br.com.survey.hotelsurvey.repository.FormularioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FormularioService {


    @Autowired
    private FormularioRepository formularioRepository;

    @Transactional
    public Formulario criar(Formulario formulario) {
        if (formularioRepository.existsByNomeAndEmpresaIdAndIdioma(formulario.getNome(), formulario.getEmpresa().getId(), formulario.getIdioma())) {
            throw new IllegalArgumentException("Este formulário já existe para a empresa e idioma selecionados.");
        }

        // garante a relação bidirecional para o JPA salvar corretamente as entidades filhas
        formulario.getPerguntas().forEach(pergunta -> {
            pergunta.setFormulario(formulario);
            if (pergunta.getOpcoes() != null) {
                pergunta.getOpcoes().forEach(opcao -> opcao.setPergunta(pergunta));
            }
        });

        return formularioRepository.save(formulario);
    }

    @Transactional
    public void desativar(Long id) {
        Formulario formulario = formularioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formulário não encontrado com id: " + id));
        formulario.setAtivo(false);
        formularioRepository.save(formulario);
    }

    public Formulario buscarComDetalhes(Long id) {
        return formularioRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Formulário não encontrado com id: " + id));
    }
}
