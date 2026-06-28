package br.edu.atitus.authservice.controllers;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.atitus.authservice.dtos.WalletAmountDTO;
import br.edu.atitus.authservice.dtos.WalletBalanceDTO;
import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.services.UserService;

@RestController
@RequestMapping("/auth/wallet")
public class WalletController {

    private final UserService userService;

    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceDTO> getBalance(@AuthenticationPrincipal UserEntity user) throws Exception {
        if (user == null) {
            throw new Exception("Usuário não autenticado");
        }

        return ResponseEntity.ok(toBalanceDto(user));
    }

    @PostMapping("/debit")
    public ResponseEntity<WalletBalanceDTO> debit(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody WalletAmountDTO dto) throws Exception {
        if (user == null) {
            throw new Exception("Usuário não autenticado");
        }

        var updated = userService.debit(user.getId(), dto.amount());
        return ResponseEntity.ok(toBalanceDto(updated));
    }

    @PostMapping("/credit")
    public ResponseEntity<WalletBalanceDTO> credit(
            @AuthenticationPrincipal UserEntity user,
            @RequestBody WalletAmountDTO dto) throws Exception {
        if (user == null) {
            throw new Exception("Usuário não autenticado");
        }

        var updated = userService.credit(user.getId(), dto.amount());
        return ResponseEntity.ok(toBalanceDto(updated));
    }

    private WalletBalanceDTO toBalanceDto(UserEntity user) {
        return new WalletBalanceDTO(
                user.getId(),
                user.getBalance() != null ? user.getBalance() : UserService.DEFAULT_BALANCE,
                "BRL",
                Instant.now().toString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        String cleanMessage = e.getMessage().replaceAll("[\\r\\n]", " ");
        return ResponseEntity.badRequest().body(cleanMessage);
    }
}
