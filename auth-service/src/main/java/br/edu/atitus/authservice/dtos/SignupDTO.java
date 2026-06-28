package br.edu.atitus.authservice.dtos;

import br.edu.atitus.authservice.entities.UserType;

public record SignupDTO(String name, String email, String password, UserType type) {
    public SignupDTO(String name, String email, String password) {
        this(name, email, password, UserType.Common);
    }
}
