package com.steel.product.trading.request;

import lombok.Data;

@Data
public class StockSearchRequest {
    private String searchText = "";
    private Integer locationId;
    private int pageNo = 1;
    private int pageSize = 10;
    private String sort = "";
}
