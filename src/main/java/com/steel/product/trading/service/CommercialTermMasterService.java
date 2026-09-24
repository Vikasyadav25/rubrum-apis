package com.steel.product.trading.service;

import java.util.List;
import org.springframework.http.ResponseEntity;
import com.steel.product.trading.entity.CommercialTermMasterEntity;
import com.steel.product.trading.request.CommercialTermRequest;
import com.steel.product.trading.request.CommercialTermSearchRequest;
import com.steel.product.trading.request.DeleteRequest;

public interface CommercialTermMasterService {
    List<CommercialTermMasterEntity> list(CommercialTermSearchRequest request);
    ResponseEntity<Object> save(CommercialTermRequest request);
    ResponseEntity<Object> delete(DeleteRequest request);
}
