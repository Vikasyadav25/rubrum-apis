package com.steel.product.trading.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PushToDispatchRequest extends BaseRequest {
    private Integer enquiryId;
    private String discussionNote;
}
