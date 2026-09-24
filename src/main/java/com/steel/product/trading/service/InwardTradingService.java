package com.steel.product.trading.service;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.steel.product.trading.request.BaseRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.InwardSearchRequest;
import com.steel.product.trading.request.InwardTradingRequest;
import com.steel.product.trading.request.SeqGeneratorRequest;

public interface InwardTradingService {

	ResponseEntity<Object> save(InwardTradingRequest inwardTradingRequest);

	Map<String, Object> getInwardList(InwardSearchRequest searchPageRequest);

	ResponseEntity<Object> inwardDelete(DeleteRequest deleteRequest);

	ResponseEntity<Object> generateSeq(SeqGeneratorRequest seqGeneratorRequest);

	ResponseEntity<Object> updateSeq(SeqGeneratorRequest seqGeneratorRequest);

	ResponseEntity<Object> getDocumentList(BaseRequest req);

	ResponseEntity<Object> getPurposeList();

}
