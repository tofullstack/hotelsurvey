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
import org.springframework.data.domain.Page; // Import the Page class
import org.springframework.data.domain.Pageable; // Import the Pageable interface
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

    private static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getLogin(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = (User) authentication.getPrincipal();

            if (!user.getActive()) {
                throw new BadCredentialsException("User is inactive.");
            }

            String jwt = jwtTokenProvider.generateToken(authentication);

            return new LoginResponse(jwt, user.getId(), user.getProfile().name(), user.getMustChangePassword());
        } catch (BadCredentialsException e) {
            throw new ValidationException("Invalid login or password.");
        }
    }

    @Transactional
    public UserDto createUser(UserCreationRequest request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new DuplicateEntryException("Login already exists.");
        }

        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            throw new ValidationException("Password does not meet the requirements: at least 8 characters, one uppercase, one lowercase, one number.");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfile(request.getProfile());
        user.setMustChangePassword(true);
        user.setActive(true);

        if (request.getCompanyId() != null) {
            Company company = companyRepository.findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + request.getCompanyId()));
            user.setCompany(company);
        }

        User savedUser = userRepository.save(user);
        return convertToUserDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (userDto.getProfile() != null) {
            user.setProfile(userDto.getProfile());
        }
        if (userDto.getCompanyId() != null) {
            Company company = companyRepository.findById(userDto.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with ID: " + userDto.getCompanyId()));
            user.setCompany(company);
        }

        User updatedUser = userRepository.save(user);
        return convertToUserDto(updatedUser);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password does not match.");
        }

        if (!PASSWORD_PATTERN.matcher(request.getNewPassword()).matches()) {
            throw new ValidationException("New password does not meet the requirements: at least 8 characters, one uppercase, one lowercase, one number.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(true);
        userRepository.save(user);
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<User> usersPage = userRepository.findAll(pageable);
        return usersPage.map(this::convertToUserDto);
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return convertToUserDto(user);
    }

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