package com.jdt16.agenin.transaction.dto.entity;

import com.jdt16.agenin.transaction.utility.ColumnNameEntityUtility;
import com.jdt16.agenin.transaction.utility.TableNameEntityUtility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = TableNameEntityUtility.TABLE_USER_BALANCE)
public class UserBalanceEntityDTO {
    @Id
    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_ID, nullable = false, updatable = false)
    private UUID userBalanceEntityDTOUserBalanceId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USERS_BALANCE_USER_ID, nullable = false, updatable = false)
    private UUID userBalanceEntityDTOUserId;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_AMOUNT, nullable = false)
    private BigDecimal userBalanceEntityDTOBalanceAmount;

    @Column(name = ColumnNameEntityUtility.COLUMN_USER_BALANCE_LAST_UPDATE, nullable = false)
    private LocalDateTime userBalanceEntityDTOBalanceLastUpdate;
}
