package com.steel.product.trading.request;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.steel.product.trading.entity.EQPTermsEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EQPRequest extends BaseRequest {

	private Integer enquiryId;

	private Integer enqCustomerId;

	private String enqEnquiryFrom;

	private Date enqEnquiryDate;;

	private BigDecimal enqQty;

	private BigDecimal enqValue;
	
	private Integer quoteCustomerId;

	private String quoteEnquiryFrom;

	private Date quoteEnquiryDate;;

	private BigDecimal quoteQty;

	private BigDecimal quoteValue;
	
	private String status;

	private List<EQPChildRequest> itemsList;
	
	private EQPTermsEntity terms;

}
