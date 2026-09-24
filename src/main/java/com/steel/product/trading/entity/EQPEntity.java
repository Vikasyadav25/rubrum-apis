package com.steel.product.trading.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "trading_eqp")
public class EQPEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "enquiry_id")
	private Integer enquiryId;

	@Column(name = "enq_customer_id")
	private Integer enqCustomerId;

	@Column(name = "enq_enquiry_from")
	private String enqEnquiryFrom;

	@Column(name = "enq_enquiry_date")
	private Date enqEnquiryDate;;

	@Column(name = "enq_qty")
	private BigDecimal enqQty;

	@Column(name = "enq_value")
	private BigDecimal enqValue;
	
	@Column(name = "quote_customer_id")
	private Integer quoteCustomerId;

	@Column(name = "quote_enquiry_from")
	private String quoteEnquiryFrom;

	@Column(name = "quote_enquiry_date")
	private Date quoteEnquiryDate;;

	@Column(name = "quote_qty")
	private BigDecimal quoteQty;

	@Column(name = "quote_value")
	private BigDecimal quoteValue;

	@Column(name = "enq_status")
	private String enqStatus;

	@Column(name = "quote_status")
	private String quoteStatus;

	@Column(name = "proforma_status")
	private String proformaStatus;

	@Column(name = "current_status")
	private String currentStatus;

	@Column(name = "do_status")
	private String dOStatus;

	@Column(name = "dc_status")
	private String dCStatus;

	@OneToMany(mappedBy = "enquiryId", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	private Set<EQPChildEntity> itemsList;
	
	@OneToOne(mappedBy = "enquiryId", cascade = { CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	private EQPTermsEntity terms = new EQPTermsEntity();

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

	public void addItem(EQPChildEntity item) {
		if (this.itemsList == null) {
			this.itemsList = new LinkedHashSet<>();
		}
		this.itemsList.add(item);
		item.setEnquiryId(this);
	}
	
	public Integer getEnquiryId() {
		return enquiryId;
	}

	public void setEnquiryId(Integer enquiryId) {
		this.enquiryId = enquiryId;
	}

	public Integer getEnqCustomerId() {
		return enqCustomerId;
	}

	public void setEnqCustomerId(Integer enqCustomerId) {
		this.enqCustomerId = enqCustomerId;
	}

	public String getEnqEnquiryFrom() {
		return enqEnquiryFrom;
	}

	public void setEnqEnquiryFrom(String enqEnquiryFrom) {
		this.enqEnquiryFrom = enqEnquiryFrom;
	}

	public Date getEnqEnquiryDate() {
		return enqEnquiryDate;
	}

	public void setEnqEnquiryDate(Date enqEnquiryDate) {
		this.enqEnquiryDate = enqEnquiryDate;
	}

	public BigDecimal getEnqQty() {
		return enqQty;
	}

	public void setEnqQty(BigDecimal enqQty) {
		this.enqQty = enqQty;
	}

	public BigDecimal getEnqValue() {
		return enqValue;
	}

	public void setEnqValue(BigDecimal enqValue) {
		this.enqValue = enqValue;
	}

	public String getEnqStatus() {
		return enqStatus;
	}

	public void setEnqStatus(String enqStatus) {
		this.enqStatus = enqStatus;
	}

	public String getQuoteStatus() {
		return quoteStatus;
	}

	public void setQuoteStatus(String quoteStatus) {
		this.quoteStatus = quoteStatus;
	}

	public String getProformaStatus() {
		return proformaStatus;
	}

	public void setProformaStatus(String proformaStatus) {
		this.proformaStatus = proformaStatus;
	}

	public String getCurrentStatus() {
		return currentStatus;
	}

	public void setCurrentStatus(String currentStatus) {
		this.currentStatus = currentStatus;
	}

	public Set<EQPChildEntity> getItemsList() {
		return itemsList;
	}

	public void setItemsList(Set<EQPChildEntity> itemsList) {
		this.itemsList = itemsList;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Integer getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Date getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(Date updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Integer getQuoteCustomerId() {
		return quoteCustomerId;
	}

	public void setQuoteCustomerId(Integer quoteCustomerId) {
		this.quoteCustomerId = quoteCustomerId;
	}

	public String getQuoteEnquiryFrom() {
		return quoteEnquiryFrom;
	}

	public void setQuoteEnquiryFrom(String quoteEnquiryFrom) {
		this.quoteEnquiryFrom = quoteEnquiryFrom;
	}

	public Date getQuoteEnquiryDate() {
		return quoteEnquiryDate;
	}

	public void setQuoteEnquiryDate(Date quoteEnquiryDate) {
		this.quoteEnquiryDate = quoteEnquiryDate;
	}

	public BigDecimal getQuoteQty() {
		return quoteQty;
	}

	public void setQuoteQty(BigDecimal quoteQty) {
		this.quoteQty = quoteQty;
	}

	public BigDecimal getQuoteValue() {
		return quoteValue;
	}

	public void setQuoteValue(BigDecimal quoteValue) {
		this.quoteValue = quoteValue;
	}

	public Integer getQuoteCreatedBy() {
		return quoteCreatedBy;
	}

	public void setQuoteCreatedBy(Integer quoteCreatedBy) {
		this.quoteCreatedBy = quoteCreatedBy;
	}

	public Integer getQuoteUpdatedBy() {
		return quoteUpdatedBy;
	}

	public void setQuoteUpdatedBy(Integer quoteUpdatedBy) {
		this.quoteUpdatedBy = quoteUpdatedBy;
	}

	public Date getQuoteCreatedOn() {
		return quoteCreatedOn;
	}

	public void setQuoteCreatedOn(Date quoteCreatedOn) {
		this.quoteCreatedOn = quoteCreatedOn;
	}

	public Date getQuoteUpdatedOn() {
		return quoteUpdatedOn;
	}

	public void setQuoteUpdatedOn(Date quoteUpdatedOn) {
		this.quoteUpdatedOn = quoteUpdatedOn;
	}

	public EQPTermsEntity getTerms() {
		return terms;
	}

	public void setTerms(EQPTermsEntity terms) {
		this.terms = terms;
	}

	public Integer getProformaCreatedBy() {
		return proformaCreatedBy;
	}

	public void setProformaCreatedBy(Integer proformaCreatedBy) {
		this.proformaCreatedBy = proformaCreatedBy;
	}

	public Integer getProformaUpdatedBy() {
		return proformaUpdatedBy;
	}

	public void setProformaUpdatedBy(Integer proformaUpdatedBy) {
		this.proformaUpdatedBy = proformaUpdatedBy;
	}

	public Date getProformaCreatedOn() {
		return proformaCreatedOn;
	}

	public void setProformaCreatedOn(Date proformaCreatedOn) {
		this.proformaCreatedOn = proformaCreatedOn;
	}

	public Date getProformaUpdatedOn() {
		return proformaUpdatedOn;
	}

	public void setProformaUpdatedOn(Date proformaUpdatedOn) {
		this.proformaUpdatedOn = proformaUpdatedOn;
	}

	public String getdOStatus() {
		return dOStatus;
	}

	public void setdOStatus(String dOStatus) {
		this.dOStatus = dOStatus;
	}

	public String getdCStatus() {
		return dCStatus;
	}

	public void setdCStatus(String dCStatus) {
		this.dCStatus = dCStatus;
	}

 
}
