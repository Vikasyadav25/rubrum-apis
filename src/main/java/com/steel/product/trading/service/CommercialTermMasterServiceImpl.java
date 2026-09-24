package com.steel.product.trading.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.steel.product.trading.entity.CommercialTermMasterEntity;
import com.steel.product.trading.repository.CommercialTermMasterRepository;
import com.steel.product.trading.request.CommercialTermRequest;
import com.steel.product.trading.request.CommercialTermSearchRequest;
import com.steel.product.trading.request.DeleteRequest;

@Service
public class CommercialTermMasterServiceImpl implements CommercialTermMasterService {
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "PAYMENT_TERM", "TAX_TYPE", "WEIGHT_RESTRICTION", "OTHER_CONDITION");

    @Autowired
    private CommercialTermMasterRepository repository;

    @Override
    public List<CommercialTermMasterEntity> list(CommercialTermSearchRequest request) {
        String type = hasText(request.getTermType()) ? request.getTermType().trim().toUpperCase() : null;
        String search = hasText(request.getSearchText()) ? request.getSearchText().trim() : null;
        return repository.findActive(type, search);
    }

    @Override
    public ResponseEntity<Object> save(CommercialTermRequest request) {
        String type = request.getTermType() == null ? "" : request.getTermType().trim().toUpperCase();
        String name = request.getTermName() == null ? "" : request.getTermName().trim();
        if (!ALLOWED_TYPES.contains(type) || name.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"status\":\"fail\",\"message\":\"Valid term type and name are required\"}");
        }
        if (!repository.findDuplicate(type, name, request.getCommercialTermId()).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("{\"status\":\"fail\",\"message\":\"This commercial term already exists\"}");
        }

        CommercialTermMasterEntity entity = new CommercialTermMasterEntity();
        if (request.getCommercialTermId() != null) {
            Optional<CommercialTermMasterEntity> existing = repository.findByCommercialTermIdAndIsDeleted(
                request.getCommercialTermId(), false);
            if (!existing.isPresent()) return ResponseEntity.notFound().build();
            entity = existing.get();
            entity.setUpdatedBy(request.getUserId());
            entity.setUpdatedOn(new Date());
        } else {
            entity.setCreatedBy(request.getUserId());
            entity.setCreatedOn(new Date());
        }
        entity.setTermType(type);
        entity.setTermName(name);
        entity.setTaxScope(request.getTaxScope() == null ? null : request.getTaxScope().trim().toUpperCase());
        entity.setTaxRate(request.getTaxRate());
        entity.setSortOrder(request.getSortOrder() == null ? 100 : request.getSortOrder());
        entity.setIsDeleted(false);
        return ResponseEntity.ok(repository.save(entity));
    }

    @Override
    public ResponseEntity<Object> delete(DeleteRequest request) {
        repository.softDelete(request.getIds(), request.getUserId());
        return ResponseEntity.ok("{\"status\":\"success\"}");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
