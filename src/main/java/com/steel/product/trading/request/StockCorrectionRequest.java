package com.steel.product.trading.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class StockCorrectionRequest {
    private String requestId;
    private String stockId;
    private LocalDate date;
    private String auditor;
    private String consignment;
    private BigDecimal before;
    private BigDecimal verified;
    private BigDecimal add;
    private BigDecimal deduct;
    private String remarks;
}
