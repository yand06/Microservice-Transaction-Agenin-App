package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductsResponse {
    @JsonProperty("productId")
    private UUID productEntityDTOId;

    @JsonProperty("productName")
    private String productEntityDTOName;

    @JsonProperty("productCode")
    private String productEntityDTOCode;

    @JsonProperty("productDesc")
    private String productEntityDTODesc;

    @JsonProperty("productPrice")
    private BigDecimal productEntityDTOPrice;

    @JsonProperty("commissionsValue")
    private BigDecimal commissionsEntityDTOValue;
}
