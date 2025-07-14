package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.model.Formulario;
import br.com.survey.hotelsurvey.service.FormularioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/formularios")
@PreAuthorize("hasAuthority('ADMIN')")
public class FormularioController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private br.com.survey.hotelsurvey.repository.FormularioRepository formularioRepository;

    @PostMapping
    public ResponseEntity<Formulario> criar(@RequestBody Formulario formulario) {
        return ResponseEntity.ok(formularioService.criar(formulario));
    }

    @GetMapping
    public List<Formulario> listar() {
        return formularioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Formulario buscarParaEdicao(@PathVariable Long id) {
        return formularioService.buscarComDetalhes(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        formularioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

}
