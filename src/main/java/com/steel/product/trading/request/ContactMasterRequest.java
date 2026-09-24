package com.steel.product.trading.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactMasterRequest {
	private Integer contactId;
	private String contactName;
	private String phoneNo;
	private String alternatePhoneNo;
	private String emailId;
}
