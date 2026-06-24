package br.edu.atitus.authservice.services;

import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.entities.UserType;
import br.edu.atitus.authservice.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// UserService.save() executa:
//   1. validate(user) — verifica nome, email (via Validator), senha, duplicidade
//   2. format(user)   — encoda a senha via encoder.encode()
//   3. userRepository.save(user)

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes Unitários (Mockito)")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    // -----------------------------------------------------------------------
    // Helper — cria UserEntity válido
    // -----------------------------------------------------------------------

    private UserEntity criarUserValido() {
        UserEntity user = new UserEntity();
        user.setName("João Silva");
        user.setEmail("joao@empresa.com");
        user.setPassword("senha123");
        user.setType(UserType.Common);
        return user;
    }

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");
    }

    // -----------------------------------------------------------------------
    // Teste 1 — save com user válido: encode senha + repository.save chamado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("save com user válido deve encodar senha e chamar repository.save")
    void save_ComUserValido_DeveEncodarSenhaESalvar() throws Exception {
        System.out.println("[DEBUG] === Teste 1: save com user válido ===");

        UserEntity user = criarUserValido();

        when(userRepository.existsByEmail("joao@empresa.com")).thenReturn(false);
        when(encoder.encode("senha123")).thenReturn("$2a$10$hashEncodado");
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        userService.save(user);

        // format() modifica a senha no objeto em memória
        assertEquals("$2a$10$hashEncodado", user.getPassword(),
                "Senha deve ser encodada via encoder.encode()");
        verify(encoder, times(1)).encode("senha123");
        verify(userRepository, times(1)).save(user);

        System.out.println("[DEBUG] ✓ Senha encodada: " + user.getPassword());
        System.out.println("[DEBUG] ✓ repository.save() chamado 1x");
    }

    // -----------------------------------------------------------------------
    // Teste 2 — save com nome nulo lança Exception
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("save com nome nulo deve lançar Exception 'Nome informado inválido'")
    void save_ComNomeNulo_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 2: save com nome nulo ===");

        UserEntity user = criarUserValido();
        user.setName(null);

        Exception ex = assertThrows(Exception.class, () -> userService.save(user));
        assertEquals("Nome informado inválido", ex.getMessage());
        verifyNoInteractions(encoder, userRepository);

        System.out.println("[DEBUG] ✓ Exception lançada: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Teste 3 — save com email inválido lança Exception
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("save com email inválido deve lançar Exception 'E-mail informado inválido'")
    void save_ComEmailInvalido_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 3: save com email inválido ===");

        UserEntity user = criarUserValido();
        user.setEmail("emailsemarroba"); // não passa na validação do Validator

        Exception ex = assertThrows(Exception.class, () -> userService.save(user));
        assertEquals("E-mail informado inválido", ex.getMessage());
        verifyNoInteractions(encoder, userRepository);

        System.out.println("[DEBUG] ✓ Exception lançada: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Teste 4 — save com senha nula lança Exception
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("save com senha nula deve lançar Exception 'Senha informada inválida'")
    void save_ComSenhaNula_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 4: save com senha nula ===");

        UserEntity user = criarUserValido();
        user.setPassword(null);

        Exception ex = assertThrows(Exception.class, () -> userService.save(user));
        assertEquals("Senha informada inválida", ex.getMessage());
        verifyNoInteractions(encoder, userRepository);

        System.out.println("[DEBUG] ✓ Exception lançada: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Teste 5 — save com email duplicado (novo user, id=null) lança Exception
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("save com email já cadastrado (novo user) deve lançar Exception 'Já existe usuário com este e-mail'")
    void save_ComEmailDuplicadoNovoUser_DeveLancarException() {
        System.out.println("[DEBUG] === Teste 5: save com email duplicado (id=null) ===");

        UserEntity user = criarUserValido();
        user.setId(null); // novo user

        when(userRepository.existsByEmail("joao@empresa.com")).thenReturn(true);

        Exception ex = assertThrows(Exception.class, () -> userService.save(user));
        assertEquals("Já existe usuário com este e-mail", ex.getMessage());
        verify(userRepository, times(1)).existsByEmail("joao@empresa.com");
        verify(userRepository, never()).save(any());

        System.out.println("[DEBUG] ✓ Exception lançada: " + ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Teste 6 — loadUserByUsername com email existente retorna UserDetails
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("loadUserByUsername com email existente deve retornar UserEntity")
    void loadUserByUsername_ComEmailExistente_DeveRetornarUser() {
        System.out.println("[DEBUG] === Teste 6: loadUserByUsername — email existente ===");

        UserEntity user = criarUserValido();
        when(userRepository.findByEmail("joao@empresa.com")).thenReturn(Optional.of(user));

        var result = userService.loadUserByUsername("joao@empresa.com");

        assertNotNull(result);
        assertEquals("joao@empresa.com", result.getUsername());
        verify(userRepository, times(1)).findByEmail("joao@empresa.com");

        System.out.println("[DEBUG] ✓ loadUserByUsername retornou: " + result.getUsername());
    }

    // -----------------------------------------------------------------------
    // Teste 7 — loadUserByUsername com email inexistente lança UsernameNotFoundException
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("loadUserByUsername com email inexistente deve lançar UsernameNotFoundException")
    void loadUserByUsername_ComEmailInexistente_DeveLancarUsernameNotFoundException() {
        System.out.println("[DEBUG] === Teste 7: loadUserByUsername — email inexistente ===");

        when(userRepository.findByEmail("naoexiste@empresa.com")).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("naoexiste@empresa.com"),
                "Deve lançar UsernameNotFoundException para email não cadastrado"
        );

        verify(userRepository, times(1)).findByEmail("naoexiste@empresa.com");
        System.out.println("[DEBUG] ✓ UsernameNotFoundException lançada para email inexistente");
    }
}