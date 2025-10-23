package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserBalanceResponse {
    @JsonProperty("userId")
    private UUID userBalanceEntityDTOUserId;

    @JsonProperty("userBalanceAmount")
    private BigDecimal userBalanceEntityDTOUserAmount;

    @JsonProperty("userWalletAmount")
    private BigDecimal userBalanceEntityDTOUserWalletAmount;

    @JsonProperty("userBalanceLastUpdated")
    private LocalDateTime userBalanceEntityDTOLastUpdated;
}
