package com.steel.product.trading.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.steel.product.trading.dto.QuoteRevisionResponse;
import com.steel.product.trading.request.EQPRequest;
import com.steel.product.trading.request.QuoteRevisionActionRequest;

public interface QuoteRevisionService {
    QuoteRevisionResponse captureRevision(EQPRequest request, boolean revised);
    List<QuoteRevisionResponse> list(Integer enquiryId);
    ResponseEntity<Object> finalise(QuoteRevisionActionRequest request);
    ResponseEntity<Object> saveDiscussionNote(QuoteRevisionActionRequest request);
}
