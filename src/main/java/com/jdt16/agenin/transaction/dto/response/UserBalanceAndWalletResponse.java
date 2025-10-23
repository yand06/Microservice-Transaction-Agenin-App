package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class UserBalanceAndWalletResponse {
    @JsonProperty("userBalanceAmount")
    private BigDecimal userBalanceEntityDTOAmount;

    @JsonProperty("userWalletAmount")
    private BigDecimal userWalletEntityDTOAmount;
}
