package com.steel.product.trading.request;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OpeningStockRequest {
    private String requestId;
    private String name;
    private String code;
    private String heat;
    private String manufacturer;
    private String category;
    private Integer locationId;
    private BigDecimal weight;
    private Integer pieces;
    private BigDecimal rate;
}
