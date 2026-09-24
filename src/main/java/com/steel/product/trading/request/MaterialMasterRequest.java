package com.steel.product.trading.request;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialMasterRequest extends BaseRequest {

	private Integer itemId;

	private String itemName;

	private String itemHsnCode;

	private String itemCode;

	private String itemGrade;

	private Integer subCategoryId;

	private Integer categoryId;

	private String displayName;

	private String brandName;

	private String manufacturerName;

	private Boolean canBeProcessed;

	private String applicableProcesses;

	private String additionalParams;

	private String technicalSpecs;

	private String unitWeights;

	private String itemImage;

	private String crossSectionalImage;

	private Boolean removeItemImage;

	private Boolean removeCrossSectionalImage;

	private BigDecimal perMeter;

	private BigDecimal perFeet;

	private BigDecimal perPC;

}
