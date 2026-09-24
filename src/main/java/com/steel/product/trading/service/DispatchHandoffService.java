package com.steel.product.trading.service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.steel.product.trading.entity.EQPEntity;
import com.steel.product.trading.repository.EQPRepository;
import com.steel.product.trading.repository.QuoteRevisionRepository;
import com.steel.product.trading.request.PushToDispatchRequest;

@Service
public class DispatchHandoffService {
    private final EQPRepository enquiries;
    private final QuoteRevisionRepository revisions;
    private final com.steel.product.trading.repository.DORepository orders;

    public DispatchHandoffService(EQPRepository enquiries, QuoteRevisionRepository revisions, com.steel.product.trading.repository.DORepository orders) {
        this.enquiries = enquiries;
        this.revisions = revisions;
        this.orders = orders;
    }

    @Transactional
    public Map<String, Object> push(PushToDispatchRequest request) {
        if (request.getEnquiryId() == null || request.getUserId() == null || request.getUserId() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enquiry and user are required.");
        EQPEntity enquiry = enquiries.findForDispatchUpdate(request.getEnquiryId())
            .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proforma not found."));
        if (!"PROFORMA".equals(enquiry.getCurrentStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a proforma can be pushed to dispatch.");
        com.steel.product.trading.entity.QuoteRevisionEntity revision = revisions.findTopByEnquiryIdOrderByVersionNoDesc(request.getEnquiryId()).orElse(null);
        if (revision == null || !"APPROVED".equals(revision.getRevisionStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Finalise the quotation before pushing to dispatch.");
        if (enquiry.getTerms() == null || enquiry.getQuoteValue() == null || enquiry.getQuoteValue().signum() <= 0
                || enquiry.getItemsList() == null || enquiry.getItemsList().stream().noneMatch(i -> !Boolean.TRUE.equals(i.getIsDeleted()) && "PROFORMA".equals(i.getStatus())))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The proforma must contain items and a valid saved total.");
        if (request.getDiscussionNote() != null) {
            if (request.getDiscussionNote().length() > 1000)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internal note cannot exceed 1000 characters.");
            revision.setDiscussionNote(request.getDiscussionNote());
            revisions.save(revision);
        }
        // Preserve the approved commercial values and item status. DO creation owns the next stage change.
        if (!"PENDING_DO".equals(enquiry.getProformaStatus())) {
            enquiry.setProformaStatus("PENDING_DO");
            enquiry.setProformaUpdatedBy(request.getUserId());
            enquiry.setProformaUpdatedOn(new Date());
            enquiries.save(enquiry);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("enquiryId", enquiry.getEnquiryId());
        result.put("message", "Proforma is available in Pending Proformas.");
        return result;
    }

    @Transactional
    public Map<String, Object> approveLoading(PushToDispatchRequest request) {
        if (request.getEnquiryId() == null || request.getUserId() == null || request.getUserId() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enquiry and user are required.");
        EQPEntity enquiry = enquiries.findForDispatchUpdate(request.getEnquiryId())
            .filter(e -> !Boolean.TRUE.equals(e.getIsDeleted()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery order not found."));
        if (!"DO".equals(enquiry.getCurrentStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a pending delivery order can be approved.");
        com.steel.product.trading.entity.DOEntity order = orders.findAllEnqIds(request.getEnquiryId()).stream()
            .filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Save the delivery order first."));
        if (order.getVehicleNo() == null || order.getVehicleNo().trim().isEmpty() || order.getDeliveryDate() == null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Save vehicle and delivery date before approving loading.");
        if (!"APPROVED".equals(enquiry.getdOStatus())) {
            enquiry.setdOStatus("APPROVED");
            enquiry.setUpdatedBy(request.getUserId());
            enquiry.setUpdatedOn(new Date());
            enquiries.save(enquiry);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("message", "Loading approved. You can now create the delivery challan.");
        return result;
    }
}
