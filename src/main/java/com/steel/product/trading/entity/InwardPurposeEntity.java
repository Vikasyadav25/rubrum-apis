package com.steel.product.trading.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "trading_inward_purpose_master")
@Data
public class InwardPurposeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "purpose_id")
	private Integer purposeId;

	@Column(name = "purpose_name", nullable = false, unique = true)
	private String purposeName;

	@Column(name = "display_order", nullable = false)
	private Integer displayOrder;

	@Column(name = "is_active", nullable = false, columnDefinition = "BIT")
	private Boolean isActive;
}
