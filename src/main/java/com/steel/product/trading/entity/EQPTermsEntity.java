package com.steel.product.trading.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.util.Date;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Data
@Table(name = "trading_eqp_quote_terms")
public class EQPTermsEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "terms_id")
	private Integer termsId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "enquiryid")
	private EQPEntity enquiryId;

	@Column(name = "payment_method")
	private String paymentMethod;

	@Column(name = "weight")
	private String weight;

	@Column(name = "loading")
	private String loading;

	@Column(name = "transport_method")
	private String transportMethod;

	@Column(name = "other_charges_method")
	private String otherChargesMethod;

	@Column(name = "tax_method")
	private String taxMethod;

	@Column(name = "validity")
	private String validity;

	@Column(name = "remarks")
	private String remarks;

	@Column(name = "taxable_amount")
	private BigDecimal taxableAmount;

	@Column(name = "loadinge200_per_ton")
	private BigDecimal loadinge200PerTon;

	@Column(name = "loading_qty")
	private BigDecimal loadingQty;

	@Column(name = "loading_rate")
	private BigDecimal loadingRate;

	@Column(name = "transport_charges")
	private BigDecimal transportCharges;

	@Column(name = "transport_qty")
	private BigDecimal transportQty;

	@Column(name = "transport_rate")
	private BigDecimal transportRate;

	@Column(name = "other_charges")
	private BigDecimal otherCharges;

	@Column(name = "total_taxable_amount")
	private BigDecimal totalTaxableAmount;

	@Column(name = "gst")
	private BigDecimal gst;

	@Column(name = "total_estimate")
	private BigDecimal totalEstimate;

	@Column(name = "r_o")
	private BigDecimal rAndO;

	@JsonProperty("rAndO")
	public BigDecimal getRAndO() {
		return rAndO;
	}

	@JsonProperty("rAndO")
	@JsonAlias("randO")
	public void setRAndO(BigDecimal value) {
		this.rAndO = value;
	}

	@Column(name = "status")
	private String status;

	@Column(name = "is_deleted", columnDefinition = "BIT")
	private Boolean isDeleted;

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
