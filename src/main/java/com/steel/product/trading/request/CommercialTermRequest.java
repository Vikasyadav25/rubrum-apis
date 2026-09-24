package com.steel.product.trading.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommercialTermRequest extends BaseRequest {
    private Integer commercialTermId;
    private String termType;
    private String termName;
    private String taxScope;
    private BigDecimal taxRate;
    private Integer sortOrder;
}
