package com.steel.product.trading.entity;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "trading_commercial_term_master")
@Data
public class CommercialTermMasterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commercial_term_id")
    private Integer commercialTermId;

    @Column(name = "term_type", nullable = false, length = 40)
    private String termType;

    @Column(name = "term_name", nullable = false, length = 255)
    private String termName;

    @Column(name = "tax_scope", length = 20)
    private String taxScope;

    @Column(name = "tax_rate", precision = 10, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_deleted", columnDefinition = "BIT")
    private Boolean isDeleted;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @CreationTimestamp
    @Column(name = "created_on", updatable = false)
    private Date createdOn;

    @UpdateTimestamp
    @Column(name = "updated_on")
    private Date updatedOn;
}
