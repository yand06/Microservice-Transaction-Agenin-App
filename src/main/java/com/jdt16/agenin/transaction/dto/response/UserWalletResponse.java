package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserWalletResponse {
    @JsonProperty("userBalanceAmount")
    private BigDecimal userBalanceAmount;

    @JsonProperty("userWalletAmount")
    private BigDecimal userWalletAmount;
}
