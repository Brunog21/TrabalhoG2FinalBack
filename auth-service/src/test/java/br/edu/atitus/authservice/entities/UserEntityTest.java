package br.edu.atitus.authservice.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserEntity - Testes Unitários")
class UserEntityTest {

    @Test
    @DisplayName("Instância com todos os campos nulos por padrão")
    void deveInstanciarComValoresPadrao() {
        System.out.println("[DEBUG] Criando instância de UserEntity...");

        UserEntity user = new UserEntity();

        assertNotNull(user);
        assertNull(user.getId(),       "id deve ser null");
        assertNull(user.getName(),     "name deve ser null");
        assertNull(user.getEmail(),    "email deve ser null");
        assertNull(user.getPassword(), "password deve ser null (@JsonIgnore)");
        assertNull(user.getType(),     "type deve ser null");

        System.out.println("[DEBUG] ✓ Todos os campos nulos por padrão");
    }

    @Test
    @DisplayName("setId e getId funcionam")
    void deveSetarEObterI() {
        System.out.println("[DEBUG] Testando setId/getId...");

        UserEntity user = new UserEntity();
        user.setId(10L);

        assertEquals(10L, user.getId());
        System.out.println("[DEBUG] ✓ id: " + user.getId());
    }

    @Test
    @DisplayName("setName e getName funcionam")
    void deveSetarEObterName() {
        System.out.println("[DEBUG] Testando setName/getName...");

        UserEntity user = new UserEntity();
        user.setName("João Silva");

        assertEquals("João Silva", user.getName());
        System.out.println("[DEBUG] ✓ name: " + user.getName());
    }

    @Test
    @DisplayName("setEmail/getEmail funcionam e getUsername() retorna o email")
    void deveSetarEmailEGetUsernameRetornaEmail() {
        System.out.println("[DEBUG] Testando email e getUsername() (UserDetails)...");

        UserEntity user = new UserEntity();
        user.setEmail("joao@empresa.com");

        assertEquals("joao@empresa.com", user.getEmail());
        // UserDetails.getUsername() deve retornar getEmail()
        assertEquals(user.getEmail(), user.getUsername(),
                "getUsername() deve retornar o mesmo valor que getEmail()");

        System.out.println("[DEBUG] ✓ email=" + user.getEmail() + " | getUsername()=" + user.getUsername());
    }

    @Test
    @DisplayName("setPassword e getPassword funcionam (@JsonIgnore não impede acesso Java)")
    void deveSetarEObterPassword() {
        System.out.println("[DEBUG] Testando setPassword/getPassword (@JsonIgnore)...");

        UserEntity user = new UserEntity();
        user.setPassword("$2a$10$encodedHash");

        assertEquals("$2a$10$encodedHash", user.getPassword(),
                "@JsonIgnore remove do JSON mas não impede acesso via getter");

        System.out.println("[DEBUG] ✓ password acessível via getter mesmo com @JsonIgnore");
    }

    @Test
    @DisplayName("setType e getType funcionam com Admin e Common")
    void deveSetarEObterType() {
        System.out.println("[DEBUG] Testando setType/getType com UserType enum...");

        UserEntity admin = new UserEntity();
        admin.setType(UserType.Admin);

        UserEntity common = new UserEntity();
        common.setType(UserType.Common);

        assertEquals(UserType.Admin,  admin.getType());
        assertEquals(UserType.Common, common.getType());
        assertEquals(0, admin.getType().ordinal(),  "Admin.ordinal() deve ser 0");
        assertEquals(1, common.getType().ordinal(), "Common.ordinal() deve ser 1");

        System.out.println("[DEBUG] ✓ Admin.ordinal()=" + admin.getType().ordinal()
                + " | Common.ordinal()=" + common.getType().ordinal());
    }

    @Test
    @DisplayName("getAuthorities() retorna lista vazia e UserDetails booleans retornam true por padrão")
    void deveRetornarAuthoritiesVaziaEBoleansTrue() {
        System.out.println("[DEBUG] Testando implementação UserDetails...");

        UserEntity user = new UserEntity();

        // getAuthorities() anotado com @JsonIgnore — retorna lista vazia
        assertNotNull(user.getAuthorities(), "getAuthorities() não deve ser null");
        assertTrue(user.getAuthorities().isEmpty(),
                "getAuthorities() deve retornar lista vazia (sem roles definidas)");

        // Spring Security UserDetails: default true para todos os flags
        assertTrue(user.isEnabled(),              "isEnabled() deve ser true por padrão");
        assertTrue(user.isAccountNonExpired(),    "isAccountNonExpired() deve ser true por padrão");
        assertTrue(user.isAccountNonLocked(),     "isAccountNonLocked() deve ser true por padrão");
        assertTrue(user.isCredentialsNonExpired(),"isCredentialsNonExpired() deve ser true por padrão");

        System.out.println("[DEBUG] ✓ getAuthorities()=[] | enabled=true | accountNonExpired=true | "
                + "accountNonLocked=true | credentialsNonExpired=true");
    }
}