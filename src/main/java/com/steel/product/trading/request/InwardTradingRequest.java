package com.steel.product.trading.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InwardTradingRequest extends BaseRequest {

	private Integer inwardId;

	private String inwardNo;

	private String status;

	private String purposeType;

	private Integer purposeId;

	private String consignmentId;

	private Integer vendorId;

	private String transporterName;

	private String transporterPhoneNo;

	private List<InwardTradingItemRequest> itemsList;

	private String vendorBatchNo;

	private Integer locationId;

	private String vehicleNo;

	private String documentNo;

	private String documentType;

	private Date documentDate;

	private String ewayBillNo;

	private Date ewayBillDate;

	private BigDecimal valueOfGoods;

	private String currencyCode;

	private String extraChargesOption;

	private BigDecimal freightCharges;

	private BigDecimal insuranceAmount;

	private BigDecimal loadingCharges;

	private BigDecimal weightmenCharges;

	private BigDecimal cgst;

	private BigDecimal sgst;

	private BigDecimal igst;

	private Integer totalInwardVolume;

	private BigDecimal totalWeight;

	private Integer totalVolume;

	private String reconciliationRemark;

	private Boolean deductFreight;

	private Boolean raiseDebitNote;

}
