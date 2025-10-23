package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CustomerOpenBankAccountResponse {
    @JsonProperty("productName")
    private String customerOpenBankAccountProductName;

    @JsonProperty("productPrice")
    private BigDecimal customerOpenBankAccountProductPrice;

    @JsonProperty("customerName")
    private String customerOpenBankAccountName;

    @JsonProperty("customerIdentityNumber")
    private String customerOpenBankAccountIdentityNumber;

    @JsonProperty("customerPhoneNumber")
    private String customerOpenBankAccountPhoneNumber;

    @JsonProperty("customerEmail")
    private String customerOpenBankAccountEmail;

    @JsonProperty("customerAddress")
    private String customerOpenBankAccountAddress;

    @JsonProperty("transactionDate")
    private LocalDateTime customerOpenBankAccountTransactionDate;

    @JsonProperty("transactionStatus")
    private String customerOpenBankAccountTransactionStatus;
}
