package com.steel.product.trading.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.steel.product.trading.request.CommercialTermRequest;
import com.steel.product.trading.request.CommercialTermSearchRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.service.CommercialTermMasterService;

@RestController
@CrossOrigin
@RequestMapping({ "/trading/commercial-term" })
public class CommercialTermMasterController {
    @Autowired
    private CommercialTermMasterService service;

    @PostMapping("/list")
    public ResponseEntity<Object> list(@RequestBody CommercialTermSearchRequest request) {
        return ResponseEntity.ok(service.list(request));
    }

    @PostMapping("/save")
    public ResponseEntity<Object> save(@RequestBody CommercialTermRequest request) {
        return service.save(request);
    }

    @PutMapping("/update")
    public ResponseEntity<Object> update(@RequestBody CommercialTermRequest request) {
        return service.save(request);
    }

    @PostMapping("/delete")
    public ResponseEntity<Object> delete(@RequestBody DeleteRequest request) {
        return service.delete(request);
    }
}
