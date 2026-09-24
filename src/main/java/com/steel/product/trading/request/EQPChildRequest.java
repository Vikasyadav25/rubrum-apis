package com.steel.product.trading.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EQPChildRequest extends BaseRequest {

	private Integer enquiryChildId;

	private Integer itemId;

	private String itemSpecs;

	private String make;

	private String altMake;
	
	private Integer locationId;

	private BigDecimal qty1;

	private String unit1;

	private BigDecimal qty2;

	private String unit2;

	private BigDecimal rate;

	private String chargeableUnit;

	private BigDecimal amount;

	private String estimateDeliveryDate;

	private String remarks;

}
