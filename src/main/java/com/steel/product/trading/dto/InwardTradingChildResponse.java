package com.steel.product.trading.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class InwardTradingChildResponse {

	// child items
	private Integer itemchildId;

	private Integer itemId;
	
	private String inwardItemId;

	private String itemName;

	private String unit;

	private Integer unitVolume;

	private BigDecimal netWeight;

	private BigDecimal rate;

	private Integer volume;

	private Integer actualNoofPieces;

	private BigDecimal theoreticalWeight;

	private BigDecimal weightVariance;

	private Integer theoreticalNoofPieces;

}
