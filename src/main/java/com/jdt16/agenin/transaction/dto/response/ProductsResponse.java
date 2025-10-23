package com.jdt16.agenin.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductsResponse {
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
