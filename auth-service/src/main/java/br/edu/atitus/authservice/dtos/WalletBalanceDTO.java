package br.edu.atitus.authservice.dtos;

public record WalletBalanceDTO(
        Long userId,
        Double balance,
        String currency,
        String updatedAt
) {
}
