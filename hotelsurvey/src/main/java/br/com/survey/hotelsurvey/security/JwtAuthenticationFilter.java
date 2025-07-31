package br.com.survey.hotelsurvey.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private JwtTokenProvider tokenProvider;

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            String requestURI = request.getRequestURI(); // Adicione esta linha para logar a URI

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt); // Obtenha a autenticação
                SecurityContextHolder.getContext().setAuthentication(authentication);

                // --- ADICIONE ESTAS LINHAS DE DEBUG TEMPORARIAMENTE ---
                logger.info("Request URI: " + requestURI);
                logger.info("JWT valid and authentication set for user: " + authentication.getName());
                authentication.getAuthorities().forEach(a -> logger.info("User authority: " + a.getAuthority()));
                // --- FIM DO DEBUG TEMPORÁRIO ---

            } else {
                // --- ADICIONE ESTAS LINHAS DE DEBUG TEMPORARIAMENTE ---
                logger.info("Request URI: " + requestURI);
                if (!StringUtils.hasText(jwt)) {
                    logger.warn("JWT is missing or empty for URI: " + requestURI);
                } else {
                    logger.warn("JWT is invalid for URI: " + requestURI);
                }
                // --- FIM DO DEBUG TEMPORÁRIO ---
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context for URI: " + request.getRequestURI(), ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
