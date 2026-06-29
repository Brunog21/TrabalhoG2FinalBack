package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.dtos.ProductInDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WsProductController - Testes Unitários (Mockito)")
class WsProductControllerTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private WsProductController controller;

    private ProductInDTO dto;
    private ProductEntity savedEntity;

    @BeforeEach
    void setUp() {
        System.out.println("[DEBUG] ===== Setup do teste iniciado =====");
        dto = new ProductInDTO(
                "iPhone 15 128GB",
                "Apple",
                "iPhone 15",
                "USD",
                799.00,
                10,
                "https://example.com/iphone.jpg",
                1
        );

        savedEntity = new ProductEntity();
        savedEntity.setId(1L);
        savedEntity.setDescription("iPhone 15 128GB");
        savedEntity.setBrand("Apple");
        savedEntity.setModel("iPhone 15");
        savedEntity.setCurrency("USD");
        savedEntity.setPrice(799.00);
        savedEntity.setStock(10);
        System.out.println("[DEBUG] DTO e entity configurados");
    }

    @Test
    @DisplayName("POST com type=0 deve salvar produto com stock=10 e retornar 201")
    void postProduct_TypeAdmin_DeveSalvarERetornar201() throws Exception {
        System.out.println("[DEBUG] === Teste 1: POST type=0 (admin) ===");
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        ResponseEntity<ProductEntity> response = controller.postProduct(dto, 1L, "admin@test.com", 0);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("iPhone 15 128GB", response.getBody().getDescription());
        // stock deve ser 10 conforme lógica do controller
        verify(repository, times(1)).save(any(ProductEntity.class));
        System.out.println("[DEBUG] ✓ 201 Created, produto salvo");
    }

    @Test
    @DisplayName("POST com type != 0 deve lançar AuthenticationException")
    void postProduct_TypeNaoAdmin_DeveLancarAuthenticationException() {
        System.out.println("[DEBUG] === Teste 2: POST type=1 (não admin) ===");

        assertThrows(AuthenticationException.class,
                () -> controller.postProduct(dto, 1L, "user@test.com", 1));

        verify(repository, never()).save(any());
        System.out.println("[DEBUG] ✓ AuthenticationException lançada para type=1");
    }

    @Test
    @DisplayName("PUT com type=0 deve atualizar produto e retornar 200")
    void putProduct_TypeAdmin_DeveAtualizarERetornar200() throws Exception {
        System.out.println("[DEBUG] === Teste 3: PUT type=0 (admin) ===");
        savedEntity.setId(1L);
        when(repository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        ResponseEntity<ProductEntity> response = controller.putProduct(1L, dto, 1L, "admin@test.com", 0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(repository, times(1)).save(any(ProductEntity.class));
        System.out.println("[DEBUG] ✓ 200 OK, produto atualizado");
    }

    @Test
    @DisplayName("PUT com type != 0 deve lançar AuthenticationException")
    void putProduct_TypeNaoAdmin_DeveLancarAuthenticationException() {
        System.out.println("[DEBUG] === Teste 4: PUT type=2 (não admin) ===");

        assertThrows(AuthenticationException.class,
                () -> controller.putProduct(1L, dto, 1L, "user@test.com", 2));

        verify(repository, never()).save(any());
        System.out.println("[DEBUG] ✓ AuthenticationException lançada para type=2");
    }

    @Test
    @DisplayName("DELETE com type=0 deve deletar produto e retornar 'Excluído'")
    void deleteProduct_TypeAdmin_DeveDeletarERetornarMensagem() throws Exception {
        System.out.println("[DEBUG] === Teste 5: DELETE type=0 (admin) ===");
        doNothing().when(repository).deleteById(1L);

        ResponseEntity<String> response = controller.deleteProduct(1L, 1L, "admin@test.com", 0);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Excluído", response.getBody());
        verify(repository, times(1)).deleteById(1L);
        System.out.println("[DEBUG] ✓ 200 OK, produto excluído");
    }

    @Test
    @DisplayName("DELETE com type != 0 deve lançar AuthenticationException")
    void deleteProduct_TypeNaoAdmin_DeveLancarAuthenticationException() {
        System.out.println("[DEBUG] === Teste 6: DELETE type=3 (não admin) ===");

        assertThrows(AuthenticationException.class,
                () -> controller.deleteProduct(1L, 1L, "user@test.com", 3));

        verify(repository, never()).deleteById(any());
        System.out.println("[DEBUG] ✓ AuthenticationException lançada para type=3");
    }
}