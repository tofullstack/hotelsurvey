package br.com.survey.hotelsurvey.controller;

import br.com.survey.hotelsurvey.dto.ChangePasswordRequest;
import br.com.survey.hotelsurvey.dto.LoginRequest;
import br.com.survey.hotelsurvey.dto.LoginResponse;
import br.com.survey.hotelsurvey.dto.UserCreationRequest;
import br.com.survey.hotelsurvey.dto.UserDto;
import br.com.survey.hotelsurvey.entity.User;
import br.com.survey.hotelsurvey.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')") // only ADMIN can create users
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserDto createdUser = authService.createUser(request);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/users/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')") // only ADMIN can deactivate users
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        authService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN can activate users
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        authService.activateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()") // Any authenticated user can change their password
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal User user, @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = authService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isUser(#id)") // ADMIN or the user themselves
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = authService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}
