package com.steel.product.trading.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Data
public class StockDocument {
    private String id;
    private String requestId;
    private String kind;
    @JsonDeserialize(using = StockIntegerDeserializer.class)
    private Integer version;
    private LocalDate date;
    @JsonDeserialize(using = StockIntegerDeserializer.class)
    private Integer locationId;
    private String location;
    private String auditor;
    private String consignment;
    private List<Line> lines;

    @Data
    public static class Line {
        private String materialId;
        private String name;
        private String heat;
        private BigDecimal systemWeight;
        @JsonDeserialize(using = StockIntegerDeserializer.class)
        private Integer systemPieces;
        private BigDecimal physicalWeight;
        @JsonDeserialize(using = StockIntegerDeserializer.class)
        private Integer physicalPieces;
        private BigDecimal before;
        @JsonDeserialize(using = StockIntegerDeserializer.class)
        private Integer pieces;
        private BigDecimal verified;
        private BigDecimal add;
        private BigDecimal deduct;
        private String remarks;
    }
}
