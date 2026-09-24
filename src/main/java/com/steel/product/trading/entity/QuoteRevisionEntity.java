package com.steel.product.trading.entity;

import java.util.Date;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import org.hibernate.annotations.BatchSize;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "trading_quote_revision")
public class QuoteRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "revision_id")
    private Integer revisionId;

    @Column(name = "enquiry_id", nullable = false)
    private Integer enquiryId;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "revision_status", nullable = false, length = 40)
    private String revisionStatus;

    @Column(name = "revision_summary", length = 500)
    private String revisionSummary;

    @OneToMany(mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNo ASC")
    @BatchSize(size = 50)
    private List<QuoteRevisionItemEntity> itemsList = new ArrayList<>();

    @OneToOne(mappedBy = "revision", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private QuoteRevisionTermsEntity terms;

    @Column(name = "enq_customer_id")
    private Integer enqCustomerId;

    @Column(name = "enq_enquiry_from", columnDefinition = "TEXT")
    private String enqEnquiryFrom;

    @Column(name = "enq_enquiry_date")
    private Date enqEnquiryDate;

    @Column(name = "enq_qty", precision = 38, scale = 12)
    private BigDecimal enqQty;

    @Column(name = "enq_value", precision = 38, scale = 12)
    private BigDecimal enqValue;

    @Column(name = "quote_customer_id")
    private Integer quoteCustomerId;

    @Column(name = "quote_enquiry_from", columnDefinition = "TEXT")
    private String quoteEnquiryFrom;

    @Column(name = "quote_enquiry_date")
    private Date quoteEnquiryDate;

    @Column(name = "quote_qty", precision = 38, scale = 12)
    private BigDecimal quoteQty;

    @Column(name = "quote_value", precision = 38, scale = 12)
    private BigDecimal quoteValue;

    @Column(name = "status", columnDefinition = "TEXT")
    private String status;

    @Column(name = "snapshot_user_id")
    private Integer userId;

    @Column(name = "ip_address", columnDefinition = "TEXT")
    private String ipAddress;

    @Column(name = "request_id", columnDefinition = "TEXT")
    private String requestId;


    @Lob
    @Column(name = "discussion_note", columnDefinition = "TEXT")
    private String discussionNote;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_on", nullable = false)
    private Date createdOn;

    @Column(name = "finalised_by")
    private Integer finalisedBy;

    @Column(name = "finalised_on")
    private Date finalisedOn;
}
