package com.steel.product.trading.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.steel.product.trading.dto.QuoteRevisionResponse;
import com.steel.product.trading.entity.EQPChildEntity;
import com.steel.product.trading.entity.EQPEntity;
import com.steel.product.trading.entity.EQPTermsEntity;
import com.steel.product.trading.entity.QuoteRevisionEntity;
import com.steel.product.trading.repository.EQPRepository;
import com.steel.product.trading.repository.QuoteRevisionRepository;
import com.steel.product.trading.request.EQPChildRequest;
import com.steel.product.trading.request.EQPRequest;
import com.steel.product.trading.request.QuoteRevisionActionRequest;

@Service
public class QuoteRevisionServiceImpl implements QuoteRevisionService {

    @Autowired
    private QuoteRevisionRepository repository;

    @Autowired
    private EQPRepository eqpRepository;

    @Override
    @Transactional
    public QuoteRevisionResponse captureRevision(EQPRequest request, boolean revised) {
        Optional<QuoteRevisionEntity> latest = repository.findTopByEnquiryIdOrderByVersionNoDesc(request.getEnquiryId());
        if (latest.isPresent() && "APPROVED".equals(latest.get().getRevisionStatus())) {
            throw new IllegalStateException("The quotation has already been finalised");
        }

        QuoteRevisionEntity revision = new QuoteRevisionEntity();
        revision.setEnquiryId(request.getEnquiryId());
        revision.setVersionNo(latest.map(item -> item.getVersionNo() + 1).orElse(1));
        revision.setRevisionStatus(revised || latest.isPresent() ? "REVISED_BY_CUSTOMER" : "SENT");
        revision.setRevisionSummary(revised || latest.isPresent()
                ? "Restoring this version will set it as the active quotation for the client."
                : "Initial quotation sent to customer.");
        revision.setCreatedBy(request.getUserId());
        revision.setCreatedOn(new Date());
        QuoteRevisionMapper.store(request, revision);
        return toResponse(repository.save(revision));
    }

    @Override
    @Transactional
    public List<QuoteRevisionResponse> list(Integer enquiryId) {
        List<QuoteRevisionEntity> revisions = repository.findByEnquiryIdOrderByVersionNoDesc(enquiryId);
        if (revisions.isEmpty()) {
            Optional<EQPEntity> existingQuote = eqpRepository.findById(enquiryId);
            if (existingQuote.isPresent() && "QUOTE".equals(existingQuote.get().getCurrentStatus())) {
                captureRevision(toSnapshotRequest(existingQuote.get()), false);
                revisions = repository.findByEnquiryIdOrderByVersionNoDesc(enquiryId);
            }
        }
        List<QuoteRevisionResponse> response = new ArrayList<>();
        for (QuoteRevisionEntity entity : revisions) {
            response.add(toResponse(entity));
        }
        return response;
    }

    @Override
    @Transactional
    public ResponseEntity<Object> finalise(QuoteRevisionActionRequest request) {
        Optional<QuoteRevisionEntity> revision = repository.findByRevisionIdAndEnquiryId(
                request.getRevisionId(), request.getEnquiryId());
        if (!revision.isPresent()) {
            return new ResponseEntity<>("{\"status\":\"fail\",\"message\":\"Revision not found\"}", HttpStatus.NOT_FOUND);
        }
        Optional<QuoteRevisionEntity> latest = repository.findTopByEnquiryIdOrderByVersionNoDesc(request.getEnquiryId());
        if (!latest.isPresent() || !latest.get().getRevisionId().equals(request.getRevisionId())) {
            return new ResponseEntity<>("{\"status\":\"fail\",\"message\":\"Only the latest version can be finalised\"}", HttpStatus.CONFLICT);
        }
        QuoteRevisionEntity entity = revision.get();
        entity.setRevisionStatus("APPROVED");
        entity.setFinalisedBy(request.getUserId());
        entity.setFinalisedOn(new Date());
        repository.save(entity);
        return new ResponseEntity<>("{\"status\":\"success\",\"message\":\"Quotation version finalised\"}", HttpStatus.OK);
    }

    @Override
    @Transactional
    public ResponseEntity<Object> saveDiscussionNote(QuoteRevisionActionRequest request) {
        Optional<QuoteRevisionEntity> revision = repository.findByRevisionIdAndEnquiryId(
                request.getRevisionId(), request.getEnquiryId());
        if (!revision.isPresent()) {
            return new ResponseEntity<>("{\"status\":\"fail\",\"message\":\"Revision not found\"}", HttpStatus.NOT_FOUND);
        }
        QuoteRevisionEntity entity = revision.get();
        entity.setDiscussionNote(request.getDiscussionNote() == null ? "" : request.getDiscussionNote().trim());
        repository.save(entity);
        return new ResponseEntity<>("{\"status\":\"success\",\"message\":\"Discussion note saved\"}", HttpStatus.OK);
    }

    private QuoteRevisionResponse toResponse(QuoteRevisionEntity entity) {
        QuoteRevisionResponse response = new QuoteRevisionResponse();
        BeanUtils.copyProperties(entity, response);
        response.setSnapshot(QuoteRevisionMapper.snapshot(entity));
        return response;
    }

    private EQPRequest toSnapshotRequest(EQPEntity entity) {
        EQPRequest request = new EQPRequest();
        BeanUtils.copyProperties(entity, request);
        request.setStatus("QUOTE");
        request.setUserId(entity.getQuoteCreatedBy() != null ? entity.getQuoteCreatedBy() : entity.getCreatedBy());

        List<EQPChildRequest> items = new ArrayList<>();
        for (EQPChildEntity child : entity.getItemsList()) {
            if (Boolean.FALSE.equals(child.getIsDeleted()) && "QUOTE".equals(child.getStatus())) {
                EQPChildRequest item = new EQPChildRequest();
                BeanUtils.copyProperties(child, item);
                item.setUserId(request.getUserId());
                items.add(item);
            }
        }
        request.setItemsList(items);

        EQPTermsEntity terms = new EQPTermsEntity();
        if (entity.getTerms() != null) {
            BeanUtils.copyProperties(entity.getTerms(), terms, "enquiryId");
        }
        request.setTerms(terms);
        return request;
    }
}
