package com.steel.product.trading.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.product.trading.dto.StockDocument;
import com.steel.product.trading.dto.StockDocument.Line;
import com.steel.product.trading.dto.StockRegisterResponse.Row;
import com.steel.product.trading.repository.StockDocumentRepository;
import com.steel.product.trading.repository.StockRegisterRepository;
import com.steel.product.trading.request.StockCorrectionRequest;
import com.steel.product.trading.request.StockSearchRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class StockDocumentService {
    private final StockDocumentRepository documents;
    private final StockRegisterRepository stock;
    private final StockRegisterService corrections;
    private final ObjectMapper mapper;
    public StockDocumentService(StockDocumentRepository documents, StockRegisterRepository stock,
            StockRegisterService corrections, ObjectMapper mapper) {
        this.documents = documents; this.stock = stock; this.corrections = corrections; this.mapper = mapper;
    }

    public Map<String, Object> list(String kind, StockSearchRequest request) {
        kind(kind);
        int page = request.getPageNo();
        int size = request.getPageSize();
        String search = text(request.getSearchText(), "Search", 255, false);
        String sort = request.getSort() == null ? "" : request.getSort();
        if (!Arrays.asList("", "asc", "desc").contains(sort)) throw new IllegalArgumentException("Sort must be asc or desc.");
        if (page < 1 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page or page size (maximum 100).");
        if ("ADJUSTMENT".equals(kind)) importCorrections();
        Map<String, Object> response = new LinkedHashMap<>();
        long total = documents.count(kind, search, request.getLocationId());
        response.put("content", documents.list(kind, search, request.getLocationId(), page, size, sort));
        response.put("totalItems", total);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        response.put("currentPage", page);
        return response;
    }

    public StockDocument get(String id) {
        StockDocument result = documents.get(id(id), false);
        if (!result.getId().equals(id)) throw new IllegalArgumentException("Stock document not found.");
        return result;
    }

    public StockDocument save(StockDocument request) {
        kind(request.getKind());
        if (request.getRequestId() == null || !request.getRequestId().matches("[0-9a-fA-F-]{36}"))
            throw new IllegalArgumentException("A request UUID is required.");
        UUID.fromString(request.getRequestId());
        String hash;
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(mapper.writeValueAsBytes(request));
            StringBuilder result = new StringBuilder();
            for (byte b : digest) result.append(String.format("%02x", b));
            hash = result.toString();
        } catch (Exception e) { throw new IllegalArgumentException("Invalid document payload."); }
        Long savedId = documents.beginRequest(request.getRequestId(), hash);
        if (savedId != null) return documents.get(savedId, false);

        boolean audit = "AUDIT".equals(request.getKind());
        StockDocument old = request.getId() == null ? null : documents.get(id(request.getId()), true);
        if (old != null && (!old.getId().equals(request.getId()) || !old.getKind().equals(request.getKind())))
            throw new IllegalArgumentException("Document type does not match.");
        if (old != null && audit) throw new IllegalArgumentException("Certified audits cannot be overwritten. Create a new audit.");
        if (old != null && !Objects.equals(old.getVersion(), request.getVersion()))
            throw new IllegalArgumentException("This adjustment was edited elsewhere. Reload before saving.");
        if (request.getDate() == null || request.getDate().isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Select a valid date that is not in the future.");
        request.setAuditor(text(request.getAuditor(), "Auditor", 255, true));
        request.setConsignment(text(request.getConsignment(), "Consignment", 255, false));
        if (request.getLocationId() == null || !stock.locationExists(request.getLocationId()))
            throw new IllegalArgumentException("Select an active storage location.");
        if (old != null && !old.getLocationId().equals(request.getLocationId()))
            throw new IllegalArgumentException("An existing adjustment cannot change location.");
        if (request.getLines() == null || request.getLines().isEmpty() || request.getLines().size() > 500)
            throw new IllegalArgumentException("Select between 1 and 500 materials.");

        Map<String, Line> next = new TreeMap<>();
        Map<String, Line> previous = new TreeMap<>();
        if (old != null) for (Line line : old.getLines()) previous.put(line.getMaterialId(), line);
        for (Line line : request.getLines()) {
            if (line == null || line.getMaterialId() == null || !line.getMaterialId().matches("(OPEN|INW)-[1-9][0-9]{0,17}"))
                throw new IllegalArgumentException("Invalid material ID.");
            if (next.put(line.getMaterialId(), line) != null) throw new IllegalArgumentException("A material can only appear once.");
        }
        Set<String> ids = new TreeSet<>(next.keySet()); ids.addAll(previous.keySet());
        Map<String, Row> rows = new TreeMap<>();
        // Stable lock order makes multi-line edits atomic and avoids cross-lot deadlocks.
        for (String stockId : ids) {
            Row row = stock.lockStock(stockId);
            if (!Objects.equals(row.getLocationId(), request.getLocationId()))
                throw new IllegalArgumentException("All materials must belong to the selected location.");
            if (row.isAllocationPending()) throw new IllegalArgumentException("Resolve dispatch allocation for " + row.getCode() + " first.");
            rows.put(stockId, row);
        }
        request.setLocation(rows.values().iterator().next().getLocation());
        for (Line line : next.values()) {
            Row row = rows.get(line.getMaterialId());
            line.setName(row.getName()); line.setHeat(row.getHeat());
            line.setRemarks(text(line.getRemarks(), "Remarks", 1000, !audit && net(line).signum() != 0));
            if (audit) {
                number(line.getPhysicalWeight(), "Physical weight", 4);
                if (line.getPhysicalPieces() == null || line.getPhysicalPieces() < 0)
                    throw new IllegalArgumentException("Physical pieces must be a non-negative integer.");
                if (line.getSystemWeight() == null || line.getSystemWeight().compareTo(row.getWeight()) != 0 ||
                        !Objects.equals(line.getSystemPieces(), row.getPieces()))
                    throw new IllegalArgumentException("System stock changed. Reload the material before certifying.");
                line.setSystemWeight(row.getWeight()); line.setSystemPieces(row.getPieces());
            } else {
                number(line.getVerified(), "Verified weight", 4); number(line.getAdd(), "Addition", 4); number(line.getDeduct(), "Deduction", 4);
                Line prior = previous.get(line.getMaterialId());
                if (prior == null && (line.getBefore() == null || line.getBefore().compareTo(row.getWeight()) != 0))
                    throw new IllegalArgumentException("Stock changed. Reload the material before adjusting.");
                line.setBefore(prior == null ? row.getWeight() : prior.getBefore());
                line.setPieces(prior == null ? row.getPieces() : prior.getPieces());
            }
        }
        if (!audit && old == null && next.values().stream().allMatch(line -> net(line).signum() == 0))
            throw new IllegalArgumentException("Enter an addition or deduction.");
        request.setVersion(old == null ? 1 : old.getVersion() + 1);
        long documentId = documents.save(request, old == null ? null : id(old.getId()));
        if (!audit) for (String stockId : ids) {
            Line line = next.get(stockId);
            BigDecimal delta = net(line).subtract(net(previous.get(stockId)));
            if (delta.signum() == 0) continue;
            StockCorrectionRequest correction = new StockCorrectionRequest();
            correction.setRequestId(UUID.nameUUIDFromBytes((request.getRequestId() + ":" + stockId).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString());
            correction.setStockId(stockId); correction.setDate(request.getDate()); correction.setAuditor(request.getAuditor());
            correction.setConsignment(request.getConsignment()); correction.setBefore(rows.get(stockId).getWeight());
            correction.setVerified(line == null ? BigDecimal.ZERO : line.getVerified());
            correction.setAdd(delta.max(BigDecimal.ZERO)); correction.setDeduct(delta.negate().max(BigDecimal.ZERO));
            correction.setRemarks(line == null ? "Removed from adjustment " + old.getId() :
                (line.getRemarks().isEmpty() ? "Adjustment revision" : line.getRemarks()));
            corrections.correct(correction);
            documents.linkEffect(documentId, correction.getRequestId());
        }
        documents.finishRequest(request.getRequestId(), documentId);
        return documents.get(documentId, false);
    }

    // Upgrade earlier Register corrections into editable ledger documents without reapplying them.
    private void importCorrections() {
        for (Map<String, Object> source : documents.legacyCorrections()) {
            String stockId = (String) source.get("stock_id");
            Row row;
            try { row = stock.findStock(stockId); }
            catch (IllegalArgumentException e) {
                row = new Row(); row.setName(stockId); row.setHeat(""); row.setLocationId(0);
                row.setLocation("Unavailable source location"); row.setPieces(0);
            }
            StockDocument document = new StockDocument(); document.setKind("ADJUSTMENT"); document.setVersion(1);
            document.setDate(((java.sql.Date) source.get("correction_date")).toLocalDate());
            document.setLocationId(row.getLocationId() == null ? 0 : row.getLocationId()); document.setLocation(row.getLocation());
            document.setAuditor((String) source.get("auditor")); document.setConsignment((String) source.get("consignment"));
            Line line = new Line(); line.setMaterialId(stockId); line.setName(row.getName()); line.setHeat(row.getHeat());
            line.setBefore((BigDecimal) source.get("before_weight")); line.setPieces(row.getPieces());
            line.setVerified((BigDecimal) source.get("verified_kg")); line.setAdd((BigDecimal) source.get("add_kg"));
            line.setDeduct((BigDecimal) source.get("deduct_kg")); line.setRemarks((String) source.get("remarks"));
            document.setLines(Collections.singletonList(line));
            documents.linkEffect(documents.save(document, null), (String) source.get("request_id"));
        }
    }
    private static long id(String value) {
        if (value == null || !value.matches("(AUD|ADJ)-[1-9][0-9]{0,17}")) throw new IllegalArgumentException("Invalid document ID.");
        return Long.parseLong(value.substring(4));
    }
    private static void kind(String value) {
        if (!"AUDIT".equals(value) && !"ADJUSTMENT".equals(value)) throw new IllegalArgumentException("Invalid document type.");
    }
    private static String text(String value, String label, int max, boolean required) {
        String result = value == null ? "" : value.trim();
        if ((required && result.isEmpty()) || result.length() > max) throw new IllegalArgumentException(label + " is invalid.");
        return result;
    }
    private static BigDecimal net(Line line) {
        if (line == null) return BigDecimal.ZERO;
        if (line.getAdd() == null || line.getDeduct() == null) throw new IllegalArgumentException("Addition and deduction are required.");
        return line.getAdd().subtract(line.getDeduct());
    }
    private static void number(BigDecimal value, String label, int scale) {
        if (value == null || value.signum() < 0 || value.stripTrailingZeros().scale() > scale || value.precision() - value.scale() > 14)
            throw new IllegalArgumentException(label + " must be non-negative with at most " + scale + " decimal places.");
    }
}
