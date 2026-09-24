package com.steel.product.trading.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "trading_customer_master")
@Data
public class CustomerEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "customer_id")
	private Integer customerId;

	@Column(name = "customer_name")
	private String customerName;

	@Column(name = "customer_nickname")
	private String customerNickName;

	@Column(name = "phone_no")
	private String phoneNo;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "contact_no")
	private String contactNo;

	@Column(name = "email_id")
	private String emailId;

	@Column(name = "pan_number")
	private String panNumber;

	@Column(name = "tan_number")
	private String tanNumber;

	@Column(name = "gst_number")
	private String gstNumber;

	@Column(name = "process_tags")
	private String processTags;

	// Address

	@Column(name = "address1")
	private String address1;

	@Column(name = "address2")
	private String address2;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "pincode")
	private Integer pincode;

	// Alternate Address

	@Column(name = "alternate_address1")
	private String alternateAddress1;

	@Column(name = "alternate_address2")
	private String alternateAddress2;

	@Column(name = "alternate_city")
	private String alternateCity;

	@Column(name = "alternate_state")
	private String alternateState;

	@Column(name = "alternate_pincode")
	private Integer alternatePincode;

	@Column(name = "is_deleted", columnDefinition = "BIT")
	private Boolean isDeleted;

	@Column(name = "include_ratesin_dc")
	private String includeRatesinDc;

	@Column(name = "purchase_report")
	private String purchaseReport;

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

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "reference_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
	@Where(clause = "reference_type = 'CUSTOMER' AND is_deleted = 0")
	private List<ContactMasterEntity> additionalContacts = new ArrayList<>();

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "reference_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
	@Where(clause = "reference_type = 'CUSTOMER' AND is_deleted = 0")
	private List<AddressMasterEntity> additionalAddresses = new ArrayList<>();

}
