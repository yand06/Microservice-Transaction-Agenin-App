package com.jdt16.agenin.transaction.service.interfacing.module;


import java.math.BigDecimal;
import java.util.UUID;

public interface CommissionValueProjection {
    UUID getCommissionsEntityDTOProductId();
    BigDecimal getCommissionsEntityDTOValue();
}
