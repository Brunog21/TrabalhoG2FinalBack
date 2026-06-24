package br.edu.atitus.authservice.controllers;

import br.edu.atitus.authservice.dtos.SigninDTO;
import br.edu.atitus.authservice.dtos.SigninResponseDTO;
import br.edu.atitus.authservice.dtos.SignupDTO;
import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.entities.UserType;
import br.edu.atitus.authservice.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController - Testes Unitários (Mockito)")
class AuthControllerTest {

    @Mock
    private UserService service;

    @Mock
    private AuthenticationConfiguration authConfig;

    @InjectMocks
    private AuthController controller;

    private UserEntity userStub;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");

        userStub = new UserEntity();
        userStub.setId(1L);
        userStub.setName("Test User");
        userStub.setEmail("test@example.com");
        userStub.setType(UserType.Common);

        System.out.println("[DEBUG] UserEntity stub configurado: " + userStub.getEmail());
    }

    @Test
    @DisplayName("signup com DTO válido deve retornar 201 com user (type sempre Common)")
    void signup_ComDtoValido_DeveRetornar201() throws Exception {
        System.out.println("[DEBUG] === Teste 1: signup retorna 201 ===");

        SignupDTO dto = new SignupDTO("Test User", "test@example.com", "senha123");

        ResponseEntity<UserEntity> response = controller.signup(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test User",       response.getBody().getName());
        assertEquals("test@example.com", response.getBody().getEmail());
        // controller SEMPRE seta UserType.Common no signup
        assertEquals(UserType.Common, response.getBody().getType());
        verify(service, times(1)).save(any(UserEntity.class));

        System.out.println("[DEBUG] ✓ 201 Created | type=" + response.getBody().getType());
    }


    @Test
    @DisplayName("signup quando service.save() lança Exception deve propagar a exceção")
    void signup_QuandoServiceLancaException_DevePropagarException() throws Exception {
        System.out.println("[DEBUG] === Teste 2: signup propaga Exception do service ===");

        doThrow(new Exception("E-mail informado inválido"))
                .when(service).save(any(UserEntity.class));

        SignupDTO dto = new SignupDTO("Test User", "emailinvalido", "senha123");

        Exception ex = assertThrows(Exception.class, () -> controller.signup(dto));
        assertEquals("E-mail informado inválido", ex.getMessage());

        System.out.println("[DEBUG] ✓ Exception propagada: " + ex.getMessage());
    }


    @Test
    @DisplayName("signin com credenciais válidas deve retornar 200 com SigninResponseDTO e token")
    void signin_ComCredenciaisValidas_DeveRetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 3: signin com credenciais válidas ===");

        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockAuthManager);
        when(service.loadUserByUsername("test@example.com")).thenReturn(userStub);

        SigninDTO dto = new SigninDTO("test@example.com", "senha123");

        ResponseEntity<SigninResponseDTO> response = controller.PostSignin(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().token(), "Token JWT não deve ser null");
        assertFalse(response.getBody().token().isEmpty(), "Token JWT não deve ser vazio");
        assertEquals(userStub, response.getBody().user());

        System.out.println("[DEBUG] ✓ 200 OK | token gerado (len=" + response.getBody().token().length() + ")");
    }

    @Test
    @DisplayName("handleException(Exception) deve retornar 400 com mensagem de erro")
    void handleException_Exception_DeveRetornar400() {
        System.out.println("[DEBUG] === Teste 4: @ExceptionHandler(Exception.class) → 400 ===");

        Exception ex = new Exception("E-mail informado inválido");

        ResponseEntity<String> response = controller.handleException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("E-mail informado inválido", response.getBody());

        System.out.println("[DEBUG] ✓ 400 Bad Request | body: " + response.getBody());
    }

    @Test
    @DisplayName("handleException(AuthenticationException) deve retornar 401 com mensagem de erro")
    void handleException_AuthenticationException_DeveRetornar401() {
        System.out.println("[DEBUG] === Teste 5: @ExceptionHandler(AuthenticationException.class) → 401 ===");

        AuthenticationException authEx = new BadCredentialsException("Bad credentials");

        ResponseEntity<String> response = controller.handleException(authEx);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Bad credentials", response.getBody());

        System.out.println("[DEBUG] ✓ 401 Unauthorized | body: " + response.getBody());
    }
}