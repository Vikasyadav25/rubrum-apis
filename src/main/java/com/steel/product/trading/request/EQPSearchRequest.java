package com.steel.product.trading.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class EQPSearchRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer enquiryId;

	private Integer customerId;
	
	private String status;
	private Boolean dispatchOnly;

	private String searchText;

	private Integer pageNo;

	private Integer pageSize;

}
