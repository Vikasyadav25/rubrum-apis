package com.steel.product.trading.entity;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "trading_quote_revision_terms")
public class QuoteRevisionTermsEntity {

    @Id
    @Column(name = "revision_id")
    private Integer revisionId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private QuoteRevisionEntity revision;

    @Column(name = "source_terms_id")
    private Integer termsId;

    @Column(name = "payment_method", columnDefinition = "TEXT")
    private String paymentMethod;

    @Column(name = "weight", columnDefinition = "TEXT")
    private String weight;

    @Column(name = "loading", columnDefinition = "TEXT")
    private String loading;

    @Column(name = "transport_method", columnDefinition = "TEXT")
    private String transportMethod;

    @Column(name = "other_charges_method", columnDefinition = "TEXT")
    private String otherChargesMethod;

    @Column(name = "tax_method", columnDefinition = "TEXT")
    private String taxMethod;

    @Column(name = "validity", columnDefinition = "TEXT")
    private String validity;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "taxable_amount", precision = 38, scale = 12)
    private BigDecimal taxableAmount;

    @Column(name = "loading_amount", precision = 38, scale = 12)
    private BigDecimal loadinge200PerTon;

    @Column(name = "loading_qty", precision = 38, scale = 12)
    private BigDecimal loadingQty;

    @Column(name = "loading_rate", precision = 38, scale = 12)
    private BigDecimal loadingRate;

    @Column(name = "transport_charges", precision = 38, scale = 12)
    private BigDecimal transportCharges;

    @Column(name = "transport_qty", precision = 38, scale = 12)
    private BigDecimal transportQty;

    @Column(name = "transport_rate", precision = 38, scale = 12)
    private BigDecimal transportRate;

    @Column(name = "other_charges", precision = 38, scale = 12)
    private BigDecimal otherCharges;

    @Column(name = "total_taxable_amount", precision = 38, scale = 12)
    private BigDecimal totalTaxableAmount;

    @Column(name = "gst", precision = 38, scale = 12)
    private BigDecimal gst;

    @Column(name = "total_estimate", precision = 38, scale = 12)
    private BigDecimal totalEstimate;

    @Column(name = "r_o", precision = 38, scale = 12)
    private BigDecimal rAndO;

    @Column(name = "status", columnDefinition = "TEXT")
    private String status;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "quote_created_by")
    private Integer quoteCreatedBy;

    @Column(name = "quote_updated_by")
    private Integer quoteUpdatedBy;

    @Column(name = "quote_created_on")
    private Date quoteCreatedOn;

    @Column(name = "quote_updated_on")
    private Date quoteUpdatedOn;

    @Column(name = "proforma_created_by")
    private Integer proformaCreatedBy;

    @Column(name = "proforma_updated_by")
    private Integer proformaUpdatedBy;

    @Column(name = "proforma_created_on")
    private Date proformaCreatedOn;

    @Column(name = "proforma_updated_on")
    private Date proformaUpdatedOn;
}

