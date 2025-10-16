package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.ProductsEntityDTO;
import com.jdt16.agenin.transaction.service.interfacing.module.ProductCodeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MProductsRepositories extends JpaRepository<ProductsEntityDTO, UUID> {
    Optional<ProductCodeProjection> findByProductEntityDTOId(UUID productId);
}
