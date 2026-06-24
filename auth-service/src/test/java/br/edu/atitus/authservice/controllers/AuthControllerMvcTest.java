package br.edu.atitus.authservice.controllers;

import br.edu.atitus.authservice.configs.ConfigSecurity;
import br.edu.atitus.authservice.entities.UserEntity;
import br.edu.atitus.authservice.entities.UserType;
import br.edu.atitus.authservice.services.UserService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(ConfigSecurity.class)
@ActiveProfiles("test")
@DisplayName("AuthController - Testes REST com MockMvc (@WebMvcTest)")
class AuthControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private AuthenticationConfiguration authConfig;

    private static final String SIGNUP_JSON = """
            {
                "name": "Test User",
                "email": "test@example.com",
                "password": "senha123"
            }
            """;

    private static final String SIGNIN_JSON = """
            {
                "email": "test@example.com",
                "password": "senha123"
            }
            """;

    @Test
    @DisplayName("POST /auth/signup com body válido deve retornar 201 com name e email (sem password)")
    void signup_ComBodyValido_DeveRetornar201() throws Exception {
        System.out.println("[DEBUG] === Teste 1: POST /auth/signup com body válido ===");

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.type").value("Common"))
                // password tem @JsonIgnore — NÃO deve aparecer no JSON
                .andExpect(jsonPath("$.password").doesNotExist());

        System.out.println("[DEBUG] ✓ 201 Created | name, email, type presentes | password ausente (@JsonIgnore)");
    }

    @Test
    @DisplayName("POST /auth/signup quando service lança Exception deve retornar 400 com mensagem")
    void signup_QuandoServiceLancaException_DeveRetornar400() throws Exception {
        System.out.println("[DEBUG] === Teste 2: POST /auth/signup com Exception do service ===");

        given(service.save(any(UserEntity.class)))
                .willThrow(new Exception("E-mail informado inválido"));

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string("E-mail informado inválido"));

        System.out.println("[DEBUG] ✓ 400 Bad Request | body: 'E-mail informado inválido'");
    }

    @Test
    @DisplayName("POST /auth/signin com credenciais válidas deve retornar 200 com token JWT")
    void signin_ComCredenciaisValidas_DeveRetornar200ComToken() throws Exception {
        System.out.println("[DEBUG] === Teste 3: POST /auth/signin com credenciais válidas ===");

        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        given(authConfig.getAuthenticationManager()).willReturn(mockAuthManager);

        UserEntity userStub = new UserEntity();
        userStub.setId(1L);
        userStub.setEmail("test@example.com");
        userStub.setName("Test User");
        userStub.setType(UserType.Common);
        given(service.loadUserByUsername("test@example.com")).willReturn(userStub);

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNIN_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // token JWT é gerado pelo JwtUtil real (chave hardcoded no código de produção)
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.user.email").value("test@example.com"));

        System.out.println("[DEBUG] ✓ 200 OK | token JWT gerado | user.email presente");
    }

    @Test
    @DisplayName("POST /auth/signin com credenciais inválidas deve retornar 401")
    void signin_ComCredenciaisInvalidas_DeveRetornar401() throws Exception {
        System.out.println("[DEBUG] === Teste 4: POST /auth/signin com credenciais inválidas ===");

        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        given(authConfig.getAuthenticationManager()).willReturn(mockAuthManager);
        // authenticate() lança BadCredentialsException (extends AuthenticationException)
        given(mockAuthManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNIN_JSON))
                .andDo(print())
                // @ExceptionHandler(AuthenticationException.class) → 401
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));

        System.out.println("[DEBUG] ✓ 401 Unauthorized | body: 'Bad credentials'");
    }

    @Test
    @DisplayName("POST /auth/signup deve chamar service.save() exatamente uma vez")
    void signup_DeveChamarServiceSaveUmaVez() throws Exception {
        System.out.println("[DEBUG] === Teste 5: POST /auth/signup — verificação comportamental ===");

        // service.save() no-op (mock padrão)

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SIGNUP_JSON))
                .andExpect(status().isCreated());

        verify(service, times(1)).save(any(UserEntity.class));

        System.out.println("[DEBUG] ✓ service.save() chamado 1x conforme esperado");
    }
}