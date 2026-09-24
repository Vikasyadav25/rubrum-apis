package com.steel.product.trading.controller;

import com.steel.product.trading.dto.StockRegisterResponse;
import com.steel.product.trading.request.OpeningStockRequest;
import com.steel.product.trading.request.StockCorrectionRequest;
import com.steel.product.trading.request.StockSearchRequest;
import com.steel.product.trading.service.StockDocumentService;
import com.steel.product.trading.service.StockRegisterService;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/trading/stock")
public class StockRegisterController {
    private final StockRegisterService service;

    private final StockDocumentService documents;

    public StockRegisterController(StockRegisterService service, StockDocumentService documents) {
        this.service = service;
        this.documents = documents;
    }

    @PostMapping("/register/list")
    public StockRegisterResponse list(@RequestBody StockSearchRequest request) {
        return service.list(request.getSearchText() == null ? "" : request.getSearchText(),
            request.getLocationId(), request.getPageNo(), request.getPageSize(),
            request.getSort() == null ? "" : request.getSort());
    }

    @PostMapping("/verification/list")
    public Map<String, Object> verifications(@RequestBody StockSearchRequest request) {
        return documents.list("AUDIT", request);
    }

    @PostMapping("/adjustment/list")
    public Map<String, Object> adjustments(@RequestBody StockSearchRequest request) {
        return documents.list("ADJUSTMENT", request);
    }

    @GetMapping("/locations")
    public List<Map<String, Object>> locations() { return service.locations(); }

    @PostMapping("/opening")
    public Map<String, String> save(@RequestBody OpeningStockRequest request) {
        return service.saveOpening(request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", exception.getMessage()));
    }

    @PostMapping("/corrections")
    public Map<String, String> correct(@RequestBody StockCorrectionRequest request) {
        return service.correct(request);
    }
}
