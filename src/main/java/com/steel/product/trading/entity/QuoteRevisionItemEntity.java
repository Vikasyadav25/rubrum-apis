package com.steel.product.trading.entity;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "trading_quote_revision_items", uniqueConstraints =
        @UniqueConstraint(name = "uk_revision_item_line", columnNames = {"revision_id", "line_no"}))
public class QuoteRevisionItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_item_id")
    private Integer revisionItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "revision_id", nullable = false)
    private QuoteRevisionEntity revision;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "enquiry_child_id")
    private Integer enquiryChildId;

    @Column(name = "item_id")
    private Integer itemId;

    @Column(name = "item_specs", columnDefinition = "TEXT")
    private String itemSpecs;

    @Column(name = "make", columnDefinition = "TEXT")
    private String make;

    @Column(name = "alt_make", columnDefinition = "TEXT")
    private String altMake;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "qty1", precision = 38, scale = 12)
    private BigDecimal qty1;

    @Column(name = "unit1", columnDefinition = "TEXT")
    private String unit1;

    @Column(name = "qty2", precision = 38, scale = 12)
    private BigDecimal qty2;

    @Column(name = "unit2", columnDefinition = "TEXT")
    private String unit2;

    @Column(name = "rate", precision = 38, scale = 12)
    private BigDecimal rate;

    @Column(name = "chargeable_unit", columnDefinition = "TEXT")
    private String chargeableUnit;

    @Column(name = "amount", precision = 38, scale = 12)
    private BigDecimal amount;

    @Column(name = "estimate_delivery_date", columnDefinition = "TEXT")
    private String estimateDeliveryDate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "ip_address", columnDefinition = "TEXT")
    private String ipAddress;

    @Column(name = "request_id", columnDefinition = "TEXT")
    private String requestId;
}

