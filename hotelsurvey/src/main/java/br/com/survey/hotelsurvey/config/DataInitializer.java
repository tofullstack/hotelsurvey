package br.com.survey.hotelsurvey.config;

import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.entity.User;
import br.com.survey.hotelsurvey.entity.UserProfile;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdminUser(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            PasswordEncoder encoder
    ) {
        return args -> {
            String login = "admincoord";

            if (userRepository.findByLogin(login).isEmpty()) {
                // procura a empresa padrão para associar (se necessário)
                Company company = companyRepository.findByName("Empresa Padrão")
                        .orElseGet(() -> {
                            Company newCompany = new Company();
                            newCompany.setName("Empresa Padrão");
                            return companyRepository.save(newCompany);
                        });

                User admin = new User();
                admin.setLogin(login);
                admin.setPassword(encoder.encode("admin123")); // senha criptografada
                admin.setActive(true);
                admin.setMustChangePassword(true); // força troca de senha no primeiro login
                admin.setProfile(UserProfile.ADMIN);
                admin.setCompany(company); // associa com empresa criada ou existente

                userRepository.save(admin);
                System.out.println("Usuário admin criado com sucesso.");
            } else {
                System.out.println("Usuário admin já existe.");
            }
        };
    }
}
