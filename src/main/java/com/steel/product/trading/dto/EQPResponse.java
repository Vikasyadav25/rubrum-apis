package com.steel.product.trading.dto;

import java.math.BigDecimal;
import java.util.List;
import com.steel.product.trading.entity.EQPTermsEntity;
import lombok.Data;

@Data
public class EQPResponse {

	private Integer enquiryId;

	private Integer enqCustomerId;

	private String enqCustomerName;

	private String enqEnquiryFrom;

	private String enqEnquiryDate;;

	private BigDecimal enqQty;

	private BigDecimal enqValue;

	private Integer quoteCustomerId;

	private String quoteCustomerName;

	private String quoteEnquiryFrom;

	private String quoteEnquiryDate;;

	private BigDecimal quoteQty;

	private BigDecimal quoteValue;

	private String latestRevisionStatus;

	private String status;
	private String proformaStatus;
	private String deliveryOrderStatus;

	private EQPTermsEntity terms = new EQPTermsEntity();

	private List<EQPChildResponse> itemsList;

}
