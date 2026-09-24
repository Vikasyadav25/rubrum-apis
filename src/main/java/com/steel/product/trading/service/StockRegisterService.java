package com.steel.product.trading.service;

import com.steel.product.trading.dto.StockRegisterResponse;
import com.steel.product.trading.repository.StockRegisterRepository;
import com.steel.product.trading.request.OpeningStockRequest;
import com.steel.product.trading.request.StockCorrectionRequest;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockRegisterService {
    private final StockRegisterRepository repository;

    public StockRegisterService(StockRegisterRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public StockRegisterResponse list(String search, Integer locationId, int page, int size, String sort) {
        if (page < 1 || size < 1 || size > 500) {
            throw new IllegalArgumentException("Page must be positive and page size must be between 1 and 500.");
        }
        if (!Arrays.asList("", "asc", "desc").contains(sort)) {
            throw new IllegalArgumentException("Sort must be asc or desc.");
        }
        if (search.length() > 255) throw new IllegalArgumentException("Search must be at most 255 characters.");
        return repository.list(search.trim(), locationId, page, size, sort);
    }

    public List<Map<String, Object>> locations() { return repository.locations(); }

    @Transactional
    public Map<String, String> saveOpening(OpeningStockRequest request) {
        try {
            if (request.getRequestId() == null || request.getRequestId().length() != 36) {
                throw new IllegalArgumentException();
            }
            UUID.fromString(request.getRequestId());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("A valid requestId UUID is required.");
        }
        request.setName(required(request.getName(), "Material name", 255));
        request.setCode(required(request.getCode(), "Material code", 100));
        request.setHeat(required(request.getHeat(), "Heat number", 100));
        request.setManufacturer(required(request.getManufacturer(), "Manufacturer", 255));
        request.setCategory(required(request.getCategory(), "Category", 100));
        positive(request.getWeight(), "Weight", 4, 14);
        positive(request.getRate(), "Rate", 2, 16);
        if (request.getPieces() == null || request.getPieces() < 1) {
            throw new IllegalArgumentException("Number of pieces must be a positive integer.");
        }
        if (request.getLocationId() == null || !repository.locationExists(request.getLocationId())) {
            throw new IllegalArgumentException("Select an active storage location.");
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("id", repository.saveOpening(request));
        result.put("message", "Stock added successfully.");
        return result;
    }

    private static String required(String value, String label, int max) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > max) {
            throw new IllegalArgumentException(label + " is required and must be at most " + max + " characters.");
        }
        return value.trim();
    }

    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Map<String, String> correct(StockCorrectionRequest request) {
        if (request.getRequestId() == null || request.getRequestId().length() != 36) {
            throw new IllegalArgumentException("A valid requestId UUID is required.");
        }
        UUID.fromString(request.getRequestId());
        if (request.getStockId() == null || !request.getStockId().matches("(OPEN|INW)-[1-9][0-9]{0,17}")) {
            throw new IllegalArgumentException("Select a valid stock entry.");
        }
        if (request.getDate() == null) throw new IllegalArgumentException("Date is required.");
        request.setAuditor(required(request.getAuditor(), "Auditor", 255));
        request.setRemarks(required(request.getRemarks(), "Reason", 1000));
        String consignment = request.getConsignment() == null ? "" : request.getConsignment().trim();
        if (consignment.length() > 255) throw new IllegalArgumentException("Consignment must be at most 255 characters.");
        request.setConsignment(consignment);
        if (request.getBefore() == null || request.getBefore().signum() < 0 ||
                request.getBefore().stripTrailingZeros().scale() > 7 ||
                request.getBefore().precision() - request.getBefore().scale() > 14) {
            throw new IllegalArgumentException("Invalid expected stock balance.");
        }
        for (BigDecimal number : Arrays.asList(request.getVerified(), request.getAdd(), request.getDeduct())) {
            if (number == null || number.signum() < 0 || number.stripTrailingZeros().scale() > 4 ||
                    number.precision() - number.scale() > 14) {
                throw new IllegalArgumentException("Weights must be non-negative with at most four decimal places.");
            }
        }
        if (request.getAdd().compareTo(request.getDeduct()) == 0) {
            throw new IllegalArgumentException("Enter an addition or deduction before saving.");
        }
        StockRegisterResponse.Row row = repository.lockStock(request.getStockId());
        if (repository.correctionExists(request)) {
            return Collections.singletonMap("message", "Stock adjustment already saved.");
        }
        if (row.isAllocationPending()) {
            throw new IllegalArgumentException("Allocate dispatched stock to its inward lot before adjusting this entry.");
        }
        if (row.getWeight().compareTo(request.getBefore()) != 0) {
            throw new IllegalArgumentException("Stock has changed. Refresh the register before adjusting it.");
        }
        BigDecimal after = row.getWeight().add(request.getAdd().subtract(request.getDeduct()).movePointLeft(3));
        if (after.signum() < 0) throw new IllegalArgumentException("Deduction exceeds available stock.");
        repository.saveCorrection(request);
        return Collections.singletonMap("message", "Stock adjustment saved.");
    }

    private static void positive(BigDecimal value, String label, int scale, int integerDigits) {
        if (value == null || value.signum() <= 0 || value.stripTrailingZeros().scale() > scale ||
                value.precision() - value.scale() > integerDigits) {
            throw new IllegalArgumentException(label + " must be positive with at most " + scale + " decimal places.");
        }
    }
}
