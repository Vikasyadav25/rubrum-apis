package com.steel.product.trading.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class InwardTradingResponse {

	private Integer inwardId;

	private String inwardNo;

	private String status;

	private String purposeType;

	private Integer purposeId;

	private String consignmentId;

	private Integer vendorId;

	private String vendorName;

	private String vendorCode;

	private String transporterName;

	private String transporterPhoneNo;

	private String vendorBatchNo;

	private Integer locationId;

	private String locationName;

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

	private BigDecimal totalInwardVolume;

	private BigDecimal totalWeight;

	private BigDecimal totalVolume;

	private String reconciliationRemark;

	private Boolean deductFreight;

	private Boolean raiseDebitNote;

	private Date createdOn;

	private List<InwardTradingChildResponse> itemsList;

}
