package com.steel.product.trading.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Data
@Table(name = "trading_eqp_items")
public class EQPChildEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "enquiry_child_id")
	private Integer enquiryChildId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "enquiryid")
	private EQPEntity enquiryId;

	@Column(name = "item_id")
	private Integer itemId;

	@Column(name = "item_specs")
	private String itemSpecs;

	@Column(name = "make")
	private String make;

	@Column(name = "alt_make")
	private String altMake;

	@Column(name = "qty1")
	private BigDecimal qty1;

	@Column(name = "location_id")
	private Integer locationId;

	@Column(name = "unit1")
	private String unit1;

	@Column(name = "qty2")
	private BigDecimal qty2;

	@Column(name = "unit2")
	private String unit2;
	
	@Column(name = "rate")
	private BigDecimal rate;

	@Column(name = "chargeable_unit")
	private String chargeableUnit;
	
	@Column(name = "amount")
	private BigDecimal amount;

	@Column(name = "estimate_delivery_date")
	private String estimateDeliveryDate;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "status")
	private String status;

	@Column(name = "is_deleted", columnDefinition = "BIT")
	private Boolean isDeleted;

	@Column(name = "created_by")
	private Integer createdBy;

	@Column(name = "updated_by")
	private Integer updatedBy;

	@Column(name = "created_on", updatable = false)
	@CreationTimestamp
	private Date createdOn;

	@Column(name = "updated_on")
	@UpdateTimestamp
	private Date updatedOn;

	@Column(name = "quote_created_by")
	private Integer quoteCreatedBy;

	@Column(name = "quote_updated_by")
	private Integer quoteUpdatedBy;

	@Column(name = "quote_created_on", updatable = false)
	@CreationTimestamp
	private Date quoteCreatedOn;

	@Column(name = "quote_updated_on")
	@UpdateTimestamp
	private Date quoteUpdatedOn;
	
	@Column(name = "proforma_created_by")
	private Integer proformaCreatedBy;

	@Column(name = "proforma_updated_by")
	private Integer proformaUpdatedBy;

	@Column(name = "proforma_created_on", updatable = false)
	@CreationTimestamp
	private Date proformaCreatedOn;

	@Column(name = "proforma_updated_on")
	@UpdateTimestamp
	private Date proformaUpdatedOn;

}
