package com.steel.product.trading.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressMasterRequest {
	private Integer addressId;
	private String address1;
	private String address2;
	private String city;
	private String state;
	private String pincode;
}
