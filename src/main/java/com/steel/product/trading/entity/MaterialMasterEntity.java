package com.steel.product.trading.entity;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "trading_material_master")
@Data
public class MaterialMasterEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "item_id")
	private Integer itemId;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "item_hsn_code")
	private String itemHsnCode;

	@Column(name = "item_code")
	private String itemCode;

	@Column(name = "item_grade_id")
	private Integer itemGradeId;

	@Column(name = "item_grade")
	private String itemGrade;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private CategoryEntity categoryEntity;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subcategory_id")
	private SubCategoryEntity subCategoryEntity;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "brand_id")
	private Integer brandId;

	@Column(name = "brand_name")
	private String brandName;

	@Column(name = "manufacturer_id")
	private Integer manufacturerId;

	@Column(name = "manufacturer_name")
	private String manufacturerName;

	@Column(name = "can_be_processed", columnDefinition = "BIT")
	private Boolean canBeProcessed;

	@Column(name = "applicable_processes", columnDefinition = "JSON")
	private String applicableProcesses;

	@Column(name = "additional_params", columnDefinition = "TEXT")
	private String additionalParams;

	@Column(name = "technical_specs", columnDefinition = "JSON")
	private String technicalSpecs;

	@Column(name = "unit_weights", columnDefinition = "JSON")
	private String unitWeights;

	@Column(name = "item_image")
	private String itemImage;

	@Column(name = "cross_sectional_image")
	private String crossSectionalImage;

	@Column(name = "per_meter")
	private BigDecimal perMeter;

	@Column(name = "per_feet")
	private BigDecimal perFeet;

	@Column(name = "per_pc")
	private BigDecimal perPC;

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

	@Transient
	private String itemImagePresignedURL;

	@Transient
	private String crossSectionalImagePresignedURL;

	@Transient
	private String materialCreationDate;

}
