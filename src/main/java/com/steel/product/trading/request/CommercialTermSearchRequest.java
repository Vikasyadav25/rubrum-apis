package com.steel.product.trading.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommercialTermSearchRequest {
    private String termType;
    private String searchText;
}
