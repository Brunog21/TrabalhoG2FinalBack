package br.edu.atitus.authservice.repositories;

import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.entities.UserType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository - Testes de Integração com H2")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    void setup() {
        System.out.println("[DEBUG] ===== Limpando banco H2 =====");
        repository.deleteAll();
    }

    private UserEntity criarUser(String name, String email) {
        UserEntity user = new UserEntity();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("$2a$10$hashedPassword"); // BCrypt simulado (não precisa ser real no repositório)
        user.setType(UserType.Common);
        return user;
    }

    @Test
    @DisplayName("save persiste UserEntity e findById recupera por ID")
    void deveSalvarEBuscarPorId() {
        System.out.println("[DEBUG] Testando save e findById...");

        UserEntity saved = repository.save(criarUser("João Silva", "joao@example.com"));

        assertNotNull(saved.getId(), "ID deve ser gerado automaticamente");
        Optional<UserEntity> found = repository.findById(saved.getId());
        assertTrue(found.isPresent(), "Deve encontrar o usuário salvo");
        assertEquals("joao@example.com", found.get().getEmail());
        assertEquals("João Silva", found.get().getName());

        System.out.println("[DEBUG] ✓ UserEntity salvo com ID=" + saved.getId());
    }

    @Test
    @DisplayName("findByEmail retorna Optional com usuário para email existente")
    void findByEmail_DeveRetornarUsuario() {
        System.out.println("[DEBUG] Testando findByEmail — email existente...");

        repository.save(criarUser("Maria Santos", "maria@example.com"));

        Optional<UserEntity> found = repository.findByEmail("maria@example.com");

        assertTrue(found.isPresent(), "Deve encontrar usuário pelo email");
        assertEquals("Maria Santos", found.get().getName());
        assertEquals("maria@example.com", found.get().getEmail());

        System.out.println("[DEBUG] ✓ findByEmail retornou usuário: " + found.get().getEmail());
    }

    @Test
    @DisplayName("findByEmail retorna Optional vazio para email não cadastrado")
    void findByEmail_DeveRetornarVazioParaEmailInexistente() {
        System.out.println("[DEBUG] Testando findByEmail — email inexistente...");

        Optional<UserEntity> found = repository.findByEmail("naoexiste@example.com");

        assertFalse(found.isPresent(), "Deve retornar Optional vazio para email inexistente");
        System.out.println("[DEBUG] ✓ Optional vazio para email não cadastrado");
    }

    @Test
    @DisplayName("existsByEmail retorna true para email já cadastrado")
    void existsByEmail_DeveRetornarTrueParaEmailExistente() {
        System.out.println("[DEBUG] Testando existsByEmail — email existente...");

        repository.save(criarUser("Carlos Lima", "carlos@example.com"));

        assertTrue(repository.existsByEmail("carlos@example.com"),
                "existsByEmail deve retornar true para email cadastrado");

        System.out.println("[DEBUG] ✓ existsByEmail=true para email cadastrado");
    }

    @Test
    @DisplayName("existsByEmail retorna false para email não cadastrado")
    void existsByEmail_DeveRetornarFalseParaEmailInexistente() {
        System.out.println("[DEBUG] Testando existsByEmail — email inexistente...");

        assertFalse(repository.existsByEmail("fantasma@example.com"),
                "existsByEmail deve retornar false para email não cadastrado");

        System.out.println("[DEBUG] ✓ existsByEmail=false para email não cadastrado");
    }

    @Test
    @DisplayName("existsByEmailAndIdNot retorna true quando outro usuário tem o mesmo email")
    void existsByEmailAndIdNot_DeveRetornarTrueParaOutroUsuario() {
        System.out.println("[DEBUG] Testando existsByEmailAndIdNot — duplicação de email...");

        UserEntity user1 = repository.save(criarUser("Ana Costa", "ana@example.com"));
        UserEntity user2 = repository.save(criarUser("Bruno Melo", "bruno@example.com"));

        assertTrue(repository.existsByEmailAndIdNot("ana@example.com", user2.getId()),
                "Deve retornar true: outro usuário já usa este email");

        System.out.println("[DEBUG] ✓ existsByEmailAndIdNot=true (email do user1 solicitado pelo user2)");
    }


    @Test
    @DisplayName("existsByEmailAndIdNot retorna false quando é o próprio usuário")
    void existsByEmailAndIdNot_DeveRetornarFalseParaProprioUsuario() {
        System.out.println("[DEBUG] Testando existsByEmailAndIdNot — próprio usuário...");

        UserEntity user = repository.save(criarUser("Pedro Alves", "pedro@example.com"));

        assertFalse(repository.existsByEmailAndIdNot("pedro@example.com", user.getId()),
                "Deve retornar false: é o próprio usuário verificando seu email");

        System.out.println("[DEBUG] ✓ existsByEmailAndIdNot=false (próprio usuário — sem conflito)");
    }
}