package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.CommissionEntityDTO;
import com.jdt16.agenin.transaction.service.interfacing.module.CommissionValueProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MCommissionRepositories extends JpaRepository<CommissionEntityDTO, UUID> {
    Optional<CommissionValueProjection> findByCommissionsEntityDTOProductId(UUID productId);
}
