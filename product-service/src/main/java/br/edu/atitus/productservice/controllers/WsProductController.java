package br.edu.atitus.productservice.controllers;

import br.edu.atitus.productservice.dtos.ProductInDTO;
import br.edu.atitus.productservice.entities.ProductEntity;
import br.edu.atitus.productservice.repositories.ProductRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;

@RestController
@RequestMapping("/ws/products")
public class WsProductController {
    private final ProductRepository repository;

    public WsProductController(ProductRepository repository) {
        this.repository = repository;
    }

    private ProductEntity convertDTOtoEntity(ProductInDTO dto){
        var entity = new ProductEntity();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    @PostMapping
    public ResponseEntity<ProductEntity> postProduct(
            @RequestBody ProductInDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Type") Integer type) throws AuthenticationException {
        if (type != 0)
            throw new AuthenticationException("Usuário sem Permissão!");

        var product = convertDTOtoEntity(dto);
        product.setCurrency("BRL");
        product.setCategoryId(dto.categoryId() != null ? dto.categoryId() : 1);
        product.setSellerId(userId);
        product.setStock(10);
        repository.save(product);
        return ResponseEntity.status(201).body(product);
    }

    @PutMapping("/{idProduct}")
    public ResponseEntity<ProductEntity> putProduct(
            @PathVariable Long idProduct,
            @RequestBody ProductInDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Type") Integer type) throws AuthenticationException {
        if (type != 0)
            throw new AuthenticationException("Usuário sem Permissão!");

        var product = convertDTOtoEntity(dto);
        product.setId(idProduct);
        product.setCurrency("BRL");
        product.setCategoryId(dto.categoryId() != null ? dto.categoryId() : 1);
        product.setSellerId(userId);
        product.setStock(10);
        repository.save(product);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{idProduct}")
    public ResponseEntity<String> deleteProduct(
            @PathVariable Long idProduct,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Email") String userEmail,
            @RequestHeader("X-User-Type") Integer type) throws AuthenticationException {
        if (type != 0)
            throw new AuthenticationException("Usuário sem Permissão!");

        repository.deleteById(idProduct);

        return ResponseEntity.ok("Excluído");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e){
        String message = e.getMessage().replace("/r/n", "");
        return ResponseEntity.badRequest().body(message);
    }
}
