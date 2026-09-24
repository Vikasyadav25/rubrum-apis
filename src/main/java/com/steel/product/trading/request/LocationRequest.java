package com.steel.product.trading.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationRequest extends BaseRequest {

	private Integer locationId;

	private String locationName;

	private String branchType;

	private String contactName;

	private String phoneNo;

	private String contactNo;

	private String emailId;

	private String address1;

	private String address2;

	private String city;

	private String state;

	private Integer pincode;

	private String gstNo;

	private List<ContactMasterRequest> additionalContacts;

}
