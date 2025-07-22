package br.com.survey.hotelsurvey.service;

import br.com.survey.hotelsurvey.dto.ChangePasswordRequest;
import br.com.survey.hotelsurvey.dto.LoginRequest;
import br.com.survey.hotelsurvey.dto.LoginResponse;
import br.com.survey.hotelsurvey.dto.UserCreationRequest;
import br.com.survey.hotelsurvey.dto.UserDto;
import br.com.survey.hotelsurvey.entity.Company;
import br.com.survey.hotelsurvey.entity.User;
import br.com.survey.hotelsurvey.entity.UserProfile;
import br.com.survey.hotelsurvey.exception.DuplicateEntryException;
import br.com.survey.hotelsurvey.exception.ResourceNotFoundException;
import br.com.survey.hotelsurvey.exception.ValidationException;
import br.com.survey.hotelsurvey.repository.CompanyRepository;
import br.com.survey.hotelsurvey.repository.UserRepository;
import br.com.survey.hotelsurvey.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CompanyRepository companyRepository;

    // A senha deve conter no mínimo: 8 caracteres, 1 letra maiúscula, 1 letra minúscula, 1 número
    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    /**
     * Autentica um usuário com base nas credenciais fornecidas.
     * @param loginRequest DTO contendo login e senha.
     * @return LoginResponse com o token JWT, ID do usuário, perfil e flag de troca de senha.
     * @throws ValidationException Se as credenciais forem inválidas.
     */


    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            // tenta autenticar o usuário
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();

            // usuários desativados não podem fazer login.
            if (!user.getActive()) {
                throw new BadCredentialsException("User is inactive.");
            }

            // gera o token JWT
            String jwt = jwtTokenProvider.generateToken(authentication);

            // retorna o token, ID, perfil e se a senha precisa ser trocada (primeiro acesso)
            return new LoginResponse(jwt, user.getId(), user.getProfile().name(), user.getMustChangePassword());
        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid login or password."); // credenciais inválidas
        }
    }

    /**
     * Cria um novo usuário no sistema.
     * Apenas usuários com perfil ADMIN podem criar novas contas.
     * Cada usuário poderá ter apenas um login exclusivo (único no sistema).
     * O sistema deve impedir a criação de logins duplicados.
     *
     * @param request DTO contendo os dados para criação do usuário.
     * @return UserDto do usuário criado.
     * @throws DuplicateEntryException Se o login já existir.
     * @throws ValidationException Se a senha não atender aos requisitos.
     * @throws ResourceNotFoundException Se a empresa não for encontrada.
     */
    @Transactional
    public UserDto createUser(UserCreationRequest request) {
        // o sistema deve impedir a criação de logins duplicados.
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new DuplicateEntryException("Login already exists.");
        }

        // validação da política de senhas
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new ValidationException("Password does not meet the requirements: at least 8 characters, one uppercase, one lowercase, one number.");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Codifica a senha
        user.setProfile(request.getProfile());
        user.setMustChangePassword(true); // ao realizar o primeiro login, o usuário será obrigado a trocar a senha padrão
        user.setActive(true); // novo usuário é ativo por padrão

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));
            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    /**
     * Permite que um usuário autenticado troque sua senha.
     * @param userId ID do usuário que está trocando a senha.
     * @param request DTO contendo a senha atual e a nova senha.
     * @throws ResourceNotFoundException Se o usuário não for encontrado.
     * @throws ValidationException Se a senha atual estiver incorreta ou a nova senha não atender aos requisitos.
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // valida a senha atual
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password does not match.");
        }

        // valida a nova senha com a política de senhas
        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new ValidationException("New password does not meet the requirements: at least 8 characters, one uppercase, one lowercase, one number.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // senha trocada, não precisa mais trocar
        userRepository.save(user);
    }

    /**
     * Desativa uma conta de usuário (soft delete).
     * Somente usuários com perfil ADMIN podem desativar contas.
     * Usuários desativados não poderão fazer login.
     * @param userId ID do usuário a ser desativado.
     * @throws ResourceNotFoundException Se o usuário não for encontrado.
     */
    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(false); // Define como inativo
        userRepository.save(user);
    }

    /**
     * Ativa uma conta de usuário.
     * Somente usuários com perfil ADMIN podem desbloquear usuários.
     * @param userId ID do usuário a ser ativado.
     * @throws ResourceNotFoundException Se o usuário não for encontrado.
     */
    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(true); // Define como ativo
        userRepository.save(user);
    }

    /**
     * Retorna a lista de todos os usuários.
     * @return Lista de UserDto.
     */
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    /**
     * Retorna um usuário pelo ID.
     * @param id ID do usuário.
     * @return UserDto do usuário.
     * @throws ResourceNotFoundException Se o usuário não for encontrado.
     */
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToUserDto(user);
    }

    // Helper para converter entidade em DTO
    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setLogin(user.getLogin());
        userDto.setActive(user.getActive());
        userDto.setProfile(user.getProfile());
        if (user.getCompany() != null) {
            userDto.setCompanyId(user.getCompany().getId());
            userDto.setCompanyName(user.getCompany().getName());
        }
        return userDto;
    }
}
