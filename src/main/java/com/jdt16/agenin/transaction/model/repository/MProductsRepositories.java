package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.ProductsEntityDTO;
import com.jdt16.agenin.transaction.dto.response.ProductsResponse;
import com.jdt16.agenin.transaction.service.interfacing.module.ProductProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MProductsRepositories extends JpaRepository<ProductsEntityDTO, UUID> {
    Optional<ProductProjection> findByProductEntityDTOId(UUID productId);
}
