package com.steel.product.trading.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.steel.product.trading.request.QuoteRevisionActionRequest;
import com.steel.product.trading.service.QuoteRevisionService;

@RestController
@CrossOrigin
@RequestMapping({ "/trading/quote/revision" })
public class QuoteRevisionController {

    @Autowired
    private QuoteRevisionService service;

    @PostMapping("/list")
    public ResponseEntity<Object> list(@RequestBody QuoteRevisionActionRequest request) {
        return ResponseEntity.ok(service.list(request.getEnquiryId()));
    }

    @PostMapping("/finalise")
    public ResponseEntity<Object> finalise(@RequestBody QuoteRevisionActionRequest request) {
        return service.finalise(request);
    }

    @PostMapping("/note")
    public ResponseEntity<Object> saveDiscussionNote(@RequestBody QuoteRevisionActionRequest request) {
        return service.saveDiscussionNote(request);
    }
}
