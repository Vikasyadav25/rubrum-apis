package com.steel.product.trading.dto;

import java.util.Date;

import com.steel.product.trading.request.EQPRequest;

import lombok.Data;

@Data
public class QuoteRevisionResponse {
    private Integer revisionId;
    private Integer enquiryId;
    private Integer versionNo;
    private String revisionStatus;
    private String revisionSummary;
    private String discussionNote;
    private Integer createdBy;
    private Date createdOn;
    private Integer finalisedBy;
    private Date finalisedOn;
    private EQPRequest snapshot;
}
