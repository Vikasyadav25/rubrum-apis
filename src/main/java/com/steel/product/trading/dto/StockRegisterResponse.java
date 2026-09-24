package com.steel.product.trading.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class StockRegisterResponse {
    private List<Row> content;
    private long totalItems;
    private int totalPages;
    private int currentPage;
    private Summary summary;

    @Data
    public static class Row {
        private String id;
        private Integer itemId;
        private String name;
        private String code;
        private String heat;
        private String manufacturer;
        private String category;
        private Integer locationId;
        private String location;
        private String date;
        private String vehicle;
        private BigDecimal incoming;
        private BigDecimal outgoing;
        private BigDecimal weight;
        private Integer pieces;
        private BigDecimal rate;
        private boolean allocationPending;
    }

    @Data
    public static class Summary {
        private long totalItems;
        private long lowStock;
        private long outOfStock;
        private BigDecimal valuation = BigDecimal.ZERO;
        private long allocationPending;
    }
}
