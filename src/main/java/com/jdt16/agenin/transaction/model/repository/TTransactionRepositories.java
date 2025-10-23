package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.TransactionEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TTransactionRepositories extends JpaRepository<TransactionEntityDTO, UUID> {
    List<TransactionEntityDTO> findByTransactionEntityDTOUserId(UUID userId);
}
