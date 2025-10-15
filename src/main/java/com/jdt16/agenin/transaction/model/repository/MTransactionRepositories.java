package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.TransactionEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MTransactionRepositories extends JpaRepository<TransactionEntityDTO, UUID> {

}
