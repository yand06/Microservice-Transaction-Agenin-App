package com.jdt16.agenin.transaction.model.repository;

import com.jdt16.agenin.transaction.dto.entity.UsersReferralEntityDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TUsersReferralRepositories extends JpaRepository<UsersReferralEntityDTO, UUID> {
    @Query("SELECT ur.usersReferralEntityDTOReferenceUserId " +
            "FROM UsersReferralEntityDTO ur " +
            "WHERE ur.usersReferralEntityDTOInviteeUserId = :inviteeUserId")
    Optional<UUID> findReferenceUserIdByInviteeUserId(@Param("inviteeUserId") UUID inviteeUserId);
}
