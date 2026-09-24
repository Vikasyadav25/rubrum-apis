package com.steel.product.trading.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "trading_contact_master")
@Getter
@Setter
public class ContactMasterEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "contact_id")
	private Integer contactId;

	@Column(name = "reference_type", nullable = false)
	private String referenceType;

	@Column(name = "reference_id", nullable = false)
	private Integer referenceId;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "phone_no")
	private String phoneNo;

	@Column(name = "alternate_phone_no")
	private String alternatePhoneNo;

	@Column(name = "email_id")
	private String emailId;

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
}
