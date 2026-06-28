package br.edu.atitus.authservice.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.authservice.dtos.UpdateProfileDTO;
import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.services.UserService;

@RestController
@RequestMapping("/auth")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserEntity> getProfile(@AuthenticationPrincipal UserEntity user) throws Exception {
        if (user == null) {
            throw new Exception("Usuário não autenticado");
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserEntity> updateProfile(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody UpdateProfileDTO dto) throws Exception {
        if (user == null) {
            throw new Exception("Usuário não autenticado");
        }
        return ResponseEntity.ok(userService.updateProfile(user.getId(), dto));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        String cleanMessage = e.getMessage().replaceAll("[\\r\\n]", " ");
        return ResponseEntity.badRequest().body(cleanMessage);
    }
}
