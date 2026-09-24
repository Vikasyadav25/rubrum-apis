package com.steel.product.trading.dto;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class CategoryResponse {

	private Integer categoryId;

	private String categoryName;

	private String categoryHsnCode;

	private Boolean isDeleted;

	private Integer createdBy;

	private Integer updatedBy;

	private Date createdOn;

	private Date updatedOn;

	private List<SubCategoryResponse> subcategories;
}
