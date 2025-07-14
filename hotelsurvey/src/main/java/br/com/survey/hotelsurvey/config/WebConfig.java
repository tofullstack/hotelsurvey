// Crie este arquivo em: src/main/java/br/com/survey/hotelsurvey/config/WebConfig.java

package br.com.survey.hotelsurvey.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Classe de configuração global para a aplicação web.
 * Usada aqui para configurar o CORS (Cross-Origin Resource Sharing).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura as permissões de CORS para a API.
     * Isso é essencial para permitir que o frontend (em localhost:3000)
     * se comunique com o backend (em localhost:8080).
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica a regra para todos os endpoints da API
                .allowedOrigins("http://localhost:3000") // Permite requisições vindas DESTE endereço
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite estes métodos HTTP
                .allowedHeaders("*") // Permite todos os cabeçalhos (como Content-Type e Authorization)
                .allowCredentials(true); // Permite o envio de credenciais (cookies, tokens)
    }
}
