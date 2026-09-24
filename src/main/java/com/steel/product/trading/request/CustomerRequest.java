package com.steel.product.trading.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequest extends BaseRequest {

	private Integer customerId;

	private String customerName;

	private String customerNickName;

	private String phoneNo;

	private String contactName;

	private String contactNo;

	private String emailId;

	private String panNumber;

	private String tanNumber;

	private String gstNumber;

	private String processTags;

	// Address

	private String address1;

	private String address2;

	private String city;

	private String state;

	private Integer pincode;

	// Alternate Address

	private String alternateAddress1;

	private String alternateAddress2;

	private String alternateCity;

	private String alternateState;

	private Integer alternatePincode;
	
	private String includeRatesinDc;

	private String purchaseReport;

	private Boolean isDeleted;

	private List<ContactMasterRequest> additionalContacts;

	private List<AddressMasterRequest> additionalAddresses;

}
