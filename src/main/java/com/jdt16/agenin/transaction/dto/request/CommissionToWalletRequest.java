package com.jdt16.agenin.transaction.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CommissionToWalletRequest {
    @NotNull
    @DecimalMin(value = "0.01")
    @JsonProperty("amountToWallet")
    private BigDecimal commissionToWalletAmount;
}
