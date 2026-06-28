package br.edu.atitus.authservice.dtos;

public record UpdateProfileDTO(
        String name,
        String avatarUrl,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode
) {
}
