package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.JwtResponse;
import br.com.survey.hotelsurvey.dto.LoginRequest;
import br.com.survey.hotelsurvey.model.Usuario;
import br.com.survey.hotelsurvey.repository.UsuarioRepository;
import br.com.survey.hotelsurvey.security.JwtTokenProvider;
import br.com.survey.hotelsurvey.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

//controller para autenticação
@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getSenha()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        // --- CÓDIGO CORRIGIDO ---
        // 1. Pega o principal da autenticação, que agora é nosso UserDetailsImpl
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // 2. Pega o nosso objeto Usuario de dentro dele, SEM IR AO BANCO!
        Usuario usuario = userDetails.getUsuario();

        // A busca no repositório foi REMOVIDA
        // Usuario usuario = usuarioRepository.findByLogin(authentication.getName()).orElseThrow(...);

        // Retorna o token e outras informações úteis para o frontend
        return ResponseEntity.ok(new JwtResponse(jwt,
                usuario.getId(),
                usuario.getLogin(),
                usuario.isPrecisaTrocarSenha()));
    }
}
