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
@Table(name = "trading_location_master")
@Data
public class LocationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "location_id")
	private Integer locationId;

	@Column(name = "location_name")
	private String locationName;

	@Column(name = "branch_type")
	private String branchType;

	@Column(name = "contact_name")
	private String contactName;

	@Column(name = "phone_no")
	private String phoneNo;

	@Column(name = "contact_no")
	private String contactNo;

	@Column(name = "email_id")
	private String emailId;

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

	@Column(name = "gst_no")
	private String gstNo;

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

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "reference_id", referencedColumnName = "location_id", insertable = false, updatable = false)
	@Where(clause = "reference_type = 'LOCATION' AND is_deleted = 0")
	private List<ContactMasterEntity> additionalContacts = new ArrayList<>();

}
