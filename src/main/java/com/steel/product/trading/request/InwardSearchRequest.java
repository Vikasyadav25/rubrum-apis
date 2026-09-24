package com.steel.product.trading.request;

import java.io.Serializable;
import lombok.Data;

@Data
public class InwardSearchRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer inwardId;

	private Integer vendorId;

	private String status;

	private String searchText;

	private Integer pageNo;

	private Integer pageSize;

}
