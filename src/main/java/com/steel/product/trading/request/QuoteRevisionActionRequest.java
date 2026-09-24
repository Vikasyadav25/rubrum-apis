package com.steel.product.trading.request;

import lombok.Data;

@Data
public class QuoteRevisionActionRequest {
    private Integer enquiryId;
    private Integer revisionId;
    private Integer userId;
    private String discussionNote;
}
