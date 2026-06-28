package br.edu.atitus.order_service.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import br.edu.atitus.order_service.entities.OrderEntity;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long>{
	@EntityGraph(attributePaths = {"items"})
	Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

	@EntityGraph(attributePaths = {"items"})
	Optional<OrderEntity> findByIdAndCustomerId(Long id, Long customerId);

	@EntityGraph(attributePaths = {"items"})
	@Query(value = "SELECT o FROM OrderEntity o", countQuery = "SELECT COUNT(o) FROM OrderEntity o")
	Page<OrderEntity> findAllWithItems(Pageable pageable);
}
