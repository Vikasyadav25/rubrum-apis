package com.steel.product.trading.controller;

import com.steel.product.trading.dto.StockDocument;
import com.steel.product.trading.service.StockDocumentService;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/trading/stock/documents")
public class StockDocumentController {
    private final StockDocumentService service;
    public StockDocumentController(StockDocumentService service) { this.service = service; }
    @GetMapping("/{id}")
    public StockDocument get(@PathVariable String id) { return service.get(id); }
    @PostMapping
    public StockDocument save(@RequestBody StockDocument request) { return service.save(request); }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> invalid(IllegalArgumentException error) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", error.getMessage()));
    }
}
