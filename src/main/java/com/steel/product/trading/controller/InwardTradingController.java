package com.steel.product.trading.controller;

import com.steel.product.trading.request.BaseRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.InwardSearchRequest;
import com.steel.product.trading.request.InwardTradingRequest;
import com.steel.product.trading.request.SeqGeneratorRequest;
import com.steel.product.trading.service.InwardTradingService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Tag(name = "Inward Trading", description = "Inward Trading")
@RequestMapping({ "/trading" })
public class InwardTradingController {

	@Autowired
	private InwardTradingService inwardTradingService;

	@PostMapping(value = "/inward/save", produces = "application/json")
	public ResponseEntity<Object> save(@RequestBody InwardTradingRequest inwardTradingRequest) {
		return inwardTradingService.save(inwardTradingRequest);
	}

	@PostMapping(value = "/inward/update", produces = "application/json")
	public ResponseEntity<Object> update(@RequestBody InwardTradingRequest inwardTradingRequest) {
		return inwardTradingService.save(inwardTradingRequest);
	}

	@PostMapping({ "/inward/list" })
	public ResponseEntity<Object> getInwardList(@RequestBody InwardSearchRequest searchPageRequest) {
		Map<String, Object> response = inwardTradingService.getInwardList(searchPageRequest);
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@PostMapping(value = "/inward/delete", produces = "application/json")
	public ResponseEntity<Object> inwardDelete(@RequestBody DeleteRequest deleteRequest) {
		return inwardTradingService.inwardDelete(deleteRequest);
	}

	@PostMapping(value = "/inward/generateSeq", produces = "application/json")
	public ResponseEntity<Object> generateSeq(@RequestBody SeqGeneratorRequest seqGeneratorRequest) {
		return inwardTradingService.generateSeq(seqGeneratorRequest);
	}

	@PostMapping(value = "/inward/updateSeq", produces = "application/json")
	public ResponseEntity<Object> updateSeq(@RequestBody SeqGeneratorRequest seqGeneratorRequest) {
		return inwardTradingService.updateSeq(seqGeneratorRequest);
	}

	@PostMapping(value = "/document/list", produces = "application/json")
	public ResponseEntity<Object> getDocumentList(BaseRequest req) {
		return inwardTradingService.getDocumentList(req);
	}

	@GetMapping(value = "/inward/purpose/list", produces = "application/json")
	public ResponseEntity<Object> getPurposeList() {
		return inwardTradingService.getPurposeList();
	}

}
