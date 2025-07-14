package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.model.Usuario;
import br.com.survey.hotelsurvey.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario criar(Usuario usuario) {
        if (usuarioRepository.existsByLogin(usuario.getLogin())) {
            throw new IllegalArgumentException("Login já está em uso.");
        }
        // Criptografa a senha antes de salvar
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        // Por padrão, um novo usuário precisa trocar a senha no primeiro acesso
        usuario.setPrecisaTrocarSenha(true);
        return usuarioRepository.save(usuario);
    }

    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}
