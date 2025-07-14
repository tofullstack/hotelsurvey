// br/com/survey/hotelsurvey/setup/DataInitializer.java
package br.com.survey.hotelsurvey.setup;

import br.com.survey.hotelsurvey.model.Empresa;
import br.com.survey.hotelsurvey.model.Usuario;
import br.com.survey.hotelsurvey.repository.EmpresaRepository;
import br.com.survey.hotelsurvey.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Verifica se já existem usuários. Se sim, não faz nada.
        if (usuarioRepository.count() == 0) {
            System.out.println("Nenhum usuário encontrado, criando dados iniciais...");

            // 1. Cria a empresa principal
            Empresa empresa = new Empresa();
            empresa.setNome("Hotel Principal");
            empresaRepository.save(empresa);

            // 2. Cria o usuário administrador
            Usuario admin = new Usuario();
            admin.setLogin("admin");
            admin.setSenha(passwordEncoder.encode("senha123")); // Criptografa a senha
            admin.setPerfil(Usuario.Perfil.ADMIN);
            admin.setAtivo(true);
            admin.setPrecisaTrocarSenha(false);
            admin.setEmpresa(empresa); // Associa o usuário à empresa
            usuarioRepository.save(admin);

            System.out.println("Usuário 'admin' com senha 'senha123' criado com sucesso.");
        }
    }
}