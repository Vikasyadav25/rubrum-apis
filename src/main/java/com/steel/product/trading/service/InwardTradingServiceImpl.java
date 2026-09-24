package com.steel.product.trading.service;

import com.steel.product.trading.dto.InwardTradingChildResponse;
import com.steel.product.trading.dto.InwardTradingResponse;
import com.steel.product.trading.entity.DocumentTypeStaticEntity;
import com.steel.product.trading.entity.InwardTradingChildEntity;
import com.steel.product.trading.entity.InwardTradingEntity;
import com.steel.product.trading.entity.InwardPurposeEntity;
import com.steel.product.trading.repository.DocumentTypeStaticRepository;
import com.steel.product.trading.repository.InwardTradingChildRepository;
import com.steel.product.trading.repository.InwardTradingRepository;
import com.steel.product.trading.repository.InwardPurposeRepository;
import com.steel.product.trading.repository.SeqGeneratorRepository;
import com.steel.product.trading.request.BaseRequest;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.InwardSearchRequest;
import com.steel.product.trading.request.InwardTradingItemRequest;
import com.steel.product.trading.request.InwardTradingRequest;
import com.steel.product.trading.request.SeqGeneratorRequest;

import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@Log4j2
public class InwardTradingServiceImpl implements InwardTradingService {

	@Autowired
	InwardTradingRepository inwardTradingRepository;

	@Autowired
	InwardTradingChildRepository childRepository;

	@Autowired
	DocumentTypeStaticRepository documentTypeStaticRepository;

	@Autowired
	SeqGeneratorRepository seqGeneratorRepository;

	@Autowired
	InwardPurposeRepository inwardPurposeRepository;
	
	@Override
	@Transactional
	public ResponseEntity<Object> save(InwardTradingRequest request) {
		log.info("In InwardTradingService page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message = "Inward details saved successfully..! ";
		try {
			InwardTradingEntity inwardTradingEntity = new InwardTradingEntity();
			BeanUtils.copyProperties(request, inwardTradingEntity);
			applyPurpose(request, inwardTradingEntity);
			inwardTradingEntity.setIsDeleted( false);
			if (inwardTradingEntity.getStatus() == null || inwardTradingEntity.getStatus().trim().isEmpty()) {
				inwardTradingEntity.setStatus("DRAFT");
			}
			if (inwardTradingEntity.getCurrencyCode() == null || inwardTradingEntity.getCurrencyCode().trim().isEmpty()) {
				inwardTradingEntity.setCurrencyCode("INR");
			}
			if (inwardTradingEntity.getDeductFreight() == null) inwardTradingEntity.setDeductFreight(false);
			if (inwardTradingEntity.getRaiseDebitNote() == null) inwardTradingEntity.setRaiseDebitNote(false);
			
			Map<Integer, Integer> oldChildIdsMap = new HashMap<>();
			List<Integer> missedChildIds = new ArrayList<>();
			
			if (request.getInwardId() != null && request.getInwardId() > 0) {
				InwardTradingEntity oldEntity = null;

				Optional<InwardTradingEntity> kk = inwardTradingRepository.findById(inwardTradingEntity.getInwardId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getInwardId()>0) {
					if (inwardTradingEntity.getInwardNo() == null || inwardTradingEntity.getInwardNo().trim().isEmpty()) {
						inwardTradingEntity.setInwardNo(oldEntity.getInwardNo() != null
								? oldEntity.getInwardNo()
								: String.format("INW-%07d", oldEntity.getInwardId()));
					}
					
					//List<InwardTradingEntity> testInwardName = inwardTradingRepository.findByInwardNumber(inwardTradingEntity.getInwardNumber(), oldEntity.getInwardId());
					//if(testInwardName!=null && testInwardName.size()>0) {
						//return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Inward No already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					//}
					for(InwardTradingChildEntity childEntity : oldEntity.getItemsList()) {
						oldChildIdsMap.put(childEntity.getItemchildId(), childEntity.getItemchildId());
					}
					inwardTradingEntity.setItemsList(oldEntity.getItemsList());
					inwardTradingEntity.setUpdatedBy( request.getUserId());
					inwardTradingEntity.setUpdatedOn(new Date());
					inwardTradingEntity.setCreatedBy(oldEntity.getCreatedBy());
					inwardTradingEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Inward details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				
				// INWARD UPDATE LOGIC 				
				inwardTradingRepository.save(inwardTradingEntity);
				List<InwardTradingChildEntity> itemsList = new ArrayList<>();
				for (InwardTradingItemRequest childReq : request.getItemsList()) {
					InwardTradingChildEntity childEntity = childReq.getItemchildId() != null && childReq.getItemchildId() > 0
							? childRepository.findById(childReq.getItemchildId()).orElse(new InwardTradingChildEntity())
							: new InwardTradingChildEntity();
					Integer createdBy = childEntity.getCreatedBy();
					Date createdOn = childEntity.getCreatedOn();
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setInwardId(inwardTradingEntity);
					childEntity.setUpdatedBy(request.getUserId());
					childEntity.setUpdatedOn(new Date());
					childEntity.setCreatedBy(createdBy != null ? createdBy : request.getUserId());
					childEntity.setCreatedOn(createdOn != null ? createdOn : new Date());
					itemsList.add(childEntity);
					if (childReq.getItemchildId() != null && childReq.getItemchildId() > 0) {
						oldChildIdsMap.remove(childReq.getItemchildId());
					}
				}
				
				if(oldChildIdsMap!=null && oldChildIdsMap.size()>0) {
					missedChildIds = new ArrayList<Integer>(oldChildIdsMap.values());
				}
				//System.out.println("hi getItemsList().size - "+itemsList.size());
				//System.out.println("hi missedChildIds size - "+missedChildIds.size());
				childRepository.saveAll(itemsList);
				if (!missedChildIds.isEmpty()) {
					childRepository.deleteData(missedChildIds, request.getUserId());
				}
			} else {
				// INWARD CREATE LOGIC 				
				//List<InwardTradingEntity> testItemName = inwardTradingRepository.findByInwardNumber( inwardTradingEntity.getInwardNumber());
				//if(testItemName!=null && testItemName.size()>0) {
				//	return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Inward No already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				//}
				inwardTradingEntity.setCreatedBy(request.getUserId());
				inwardTradingEntity.setCreatedOn(new Date());

				//List<InwardTradingChildEntity> itemsList = new ArrayList<>();
				for (InwardTradingItemRequest childReq : request.getItemsList()) {
					InwardTradingChildEntity childEntity = new InwardTradingChildEntity();
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setInwardId(inwardTradingEntity);
					childEntity.setCreatedBy(request.getUserId());
					childEntity.setCreatedOn(new Date());
					inwardTradingEntity.addItem(childEntity);
				}
				inwardTradingRepository.save(inwardTradingEntity);
				if (inwardTradingEntity.getInwardNo() == null || inwardTradingEntity.getInwardNo().trim().isEmpty()) {
					inwardTradingEntity.setInwardNo(String.format("INW-%07d", inwardTradingEntity.getInwardId()));
				}
				int lineNumber = 1;
				for (InwardTradingChildEntity item : inwardTradingEntity.getItemsList()) {
					if (item.getInwardItemId() == null || item.getInwardItemId().trim().isEmpty()) {
						item.setInwardItemId(inwardTradingEntity.getInwardNo() + "-" + lineNumber);
					}
					lineNumber++;
				}
				inwardTradingRepository.save(inwardTradingEntity);
			}
			Map<String, Object> responseBody = new HashMap<>();
			responseBody.put("status", "success");
			responseBody.put("message", message.trim());
			responseBody.put("inwardId", inwardTradingEntity.getInwardId());
			responseBody.put("inwardNo", inwardTradingEntity.getInwardNo());
			responseBody.put("consignmentId", inwardTradingEntity.getConsignmentId());
			responseBody.put("inwardStatus", inwardTradingEntity.getStatus());
			response = new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.OK);
		} catch (IllegalArgumentException e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"" + e.getMessage() + "\"}", header, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Map<String, Object> getInwardList(InwardSearchRequest searchPageRequest) {
		log.info("In getInwardList page ");
		Map<String, Object> response = new HashMap<>();
		Pageable pageable = PageRequest.of((searchPageRequest.getPageNo() - 1), searchPageRequest.getPageSize());
		
		Page<Object[]> pageResult = inwardTradingRepository.findAllInwardsWithSearchText(searchPageRequest.getInwardId(), searchPageRequest.getVendorId(), searchPageRequest.getSearchText(), searchPageRequest.getStatus(), pageable);
		List<Integer> inwardIdsList = new ArrayList<>();
		for (Object[] result : pageResult) {
			inwardIdsList.add(result[0] != null ? (Integer) result[0] : null);
		}
		if (inwardIdsList.isEmpty()) {
			response.put("content", new ArrayList<InwardTradingResponse>());
			response.put("currentPage", pageResult.getNumber() + 1);
			response.put("totalItems", pageResult.getTotalElements());
			response.put("totalPages", pageResult.getTotalPages());
			return response;
		}

		Map<Integer, List<InwardTradingChildResponse>> map = new LinkedHashMap<>();
		Map<Integer, InwardTradingResponse> inwardMap = new LinkedHashMap<>();
		List<InwardTradingResponse> responseList = new ArrayList<>();
		List<Object[]> pageResultChild = inwardTradingRepository.findAllInwardsWiseData(inwardIdsList);
		for (Object[] result : pageResultChild) {
			InwardTradingResponse dto = new InwardTradingResponse();
			InwardTradingChildResponse childDTO = new InwardTradingChildResponse();
			dto.setInwardId(result[0] != null ? (Integer) result[0] : null);
			dto.setVendorName(result[1] != null ? (String) result[1] : null);
			dto.setPurposeType(result[2] != null ? (String) result[2] : null);
			dto.setVendorId(result[3] != null ? (Integer) result[3] : null);
			dto.setTransporterName(result[4] != null ? (String) result[4] : null);
			dto.setTransporterPhoneNo(result[5] != null ? (String) result[5] : null);
			dto.setVendorBatchNo(result[6] != null ? (String) result[6] : null);
			dto.setConsignmentId(result[7] != null ? (String) result[7] : null);
			dto.setLocationId(result[8] != null ? (Integer) result[8] : null);
			dto.setVehicleNo(result[9] != null ? (String) result[9] : null);
			dto.setDocumentNo(result[10] != null ? (String) result[10] : null);
			dto.setDocumentType(result[11] != null ? (String) result[11] : null);
			dto.setDocumentDate(result[12] != null ? (Date) result[12] : null);
			dto.setEwayBillNo(result[13] != null ? (String) result[13] : null);
			dto.setEwayBillDate(result[14] != null ? (Date) result[14] : null);
			dto.setValueOfGoods(result[15] != null ? (BigDecimal) result[15] : null);
			dto.setExtraChargesOption(result[16] != null ? (String) result[16] : null);
			dto.setFreightCharges(result[17] != null ? (BigDecimal) result[17] : null);
			dto.setInsuranceAmount(result[18] != null ? (BigDecimal) result[18] : null);
			dto.setLoadingCharges(result[19] != null ? (BigDecimal) result[19] : null);
			dto.setWeightmenCharges(result[20] != null ? (BigDecimal) result[20] : null);
			dto.setCgst(result[21] != null ? (BigDecimal) result[21] : null);
			dto.setSgst(result[22] != null ? (BigDecimal) result[22] : null);
			dto.setIgst(result[23] != null ? (BigDecimal) result[23] : null);
			dto.setTotalInwardVolume(result[24] != null ? (BigDecimal) result[24] : null);
			dto.setTotalWeight(result[25] != null ? (BigDecimal) result[25] : null);
			dto.setTotalVolume(result[26] != null ? (BigDecimal) result[26] : null);

			// child items
			childDTO.setItemchildId(result[27] != null ? (Integer) result[27] : null);
			childDTO.setItemId(result[28] != null ? (Integer) result[28] : null);
			childDTO.setUnit(result[29] != null ? (String) result[29] : null);
			childDTO.setUnitVolume(result[30] != null ? (Integer) result[30] : null);
			childDTO.setNetWeight(result[31] != null ? (BigDecimal) result[31] : null);
			childDTO.setRate(result[32] != null ? (BigDecimal) result[32] : null);
			childDTO.setVolume(result[33] != null ? (Integer) result[33] : null);
			childDTO.setActualNoofPieces(result[34] != null ? (Integer) result[34] : null);
			childDTO.setTheoreticalWeight(result[35] != null ? (BigDecimal) result[35] : null);
			childDTO.setWeightVariance(result[36] != null ? (BigDecimal) result[36] : null);
			childDTO.setTheoreticalNoofPieces(result[37] != null ? (Integer) result[37] : null);
			childDTO.setItemName(result[38] != null ? (String) result[38] : null);
			childDTO.setInwardItemId( result[39] != null ? (String) result[39] : null);
			dto.setInwardNo(result[40] != null ? (String) result[40] : null);
			dto.setStatus(result[41] != null ? (String) result[41] : null);
			dto.setCurrencyCode(result[42] != null ? (String) result[42] : null);
			dto.setReconciliationRemark(result[43] != null ? (String) result[43] : null);
			dto.setDeductFreight(toBoolean(result[44]));
			dto.setRaiseDebitNote(toBoolean(result[45]));
			dto.setVendorCode(result[46] != null ? (String) result[46] : null);
			dto.setLocationName(result[47] != null ? (String) result[47] : null);
			dto.setCreatedOn(result[48] != null ? (Date) result[48] : null);
			dto.setPurposeId(result[49] != null ? ((Number) result[49]).intValue() : null);

			if (map.get(dto.getInwardId()) != null) {
				List<InwardTradingChildResponse> dummyList = map.get(dto.getInwardId());
				dummyList.add(childDTO);
				map.put(dto.getInwardId(), dummyList);
			} else {
				List<InwardTradingChildResponse> dummyList = new ArrayList<>();
				dummyList.add(childDTO);
				map.put(dto.getInwardId(), dummyList);
			}
			inwardMap.put(dto.getInwardId(), dto);
		}
		for (Entry<Integer, List<InwardTradingChildResponse>> entry : map.entrySet()) {
			InwardTradingResponse dummyEntity = inwardMap.get(entry.getKey());
			dummyEntity.setItemsList(entry.getValue());
			responseList.add(dummyEntity);
		}

		response.put("content", responseList);
		response.put("currentPage", pageResult.getNumber() + 1);
		response.put("totalItems", pageResult.getTotalElements());
		response.put("totalPages", pageResult.getTotalPages());

		return response;
	}

	@Override
	@Transactional
	public ResponseEntity<Object> inwardDelete(DeleteRequest deleteRequest) {
		log.info("In inwardDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			inwardTradingRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			childRepository.deleteByInwardIds(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Inward has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private Boolean toBoolean(Object value) {
		if (value == null) return false;
		if (value instanceof Boolean) return (Boolean) value;
		if (value instanceof Number) return ((Number) value).intValue() != 0;
		return Boolean.valueOf(value.toString());
	}
	
	@Override
	public ResponseEntity<Object> generateSeq(SeqGeneratorRequest seqGeneratorRequest) {
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		try {
			RandomString kk = new RandomString();
			String mainSeq = kk.nextString();

			response = new ResponseEntity<>("{\"status\": \"success\", \"seqNo\": \"" + mainSeq + "\"}",
					new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"seqNo\": \"\"}", header,
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	/*
	@Override
	public ResponseEntity<Object> generateSeq(SeqGeneratorRequest seqGeneratorRequest) {
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		try {
			SeqGeneratorEntity skuGeneratorEntity = seqGeneratorRepository.findByFieldName(seqGeneratorRequest.getFieldName());
			int seq = skuGeneratorEntity.getCurSeq();
			String mainSeq = "";
			if (skuGeneratorEntity.getSkuFormat() != null && skuGeneratorEntity.getSkuFormat().length() > 0) {
				mainSeq = skuGeneratorEntity.getSkuFormat() + "" + String.format("%7s", seq).replace(" ", "0");
			} else {
				mainSeq = String.format("%7s", seq).replace(" ", "0");
			}
			seqGeneratorRepository.updateSKUCodeSeq(seqGeneratorRequest.getFieldName());
			response = new ResponseEntity<>( "{\"status\": \"success\", \"seqNo\": \""+mainSeq+"\"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"seqNo\": \"\"}", header,	HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}*/

	@Override
	public ResponseEntity<Object> updateSeq(SeqGeneratorRequest seqGeneratorRequest) {
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		try {
			seqGeneratorRepository.updateSKUCodeSeq(seqGeneratorRequest.getFieldName());
			response = new ResponseEntity<>( "{\"status\": \"success\", \"message\": \"updated successfully..!\"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"\"}", header,	HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public ResponseEntity<Object> getDocumentList(BaseRequest req) {
		List<DocumentTypeStaticEntity> response = documentTypeStaticRepository.findAll();
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getPurposeList() {
		List<InwardPurposeEntity> response = inwardPurposeRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	private void applyPurpose(InwardTradingRequest request, InwardTradingEntity inwardTradingEntity) {
		if (request.getPurposeId() != null) {
			InwardPurposeEntity purpose = inwardPurposeRepository.findByPurposeIdAndIsActiveTrue(request.getPurposeId())
					.orElseThrow(() -> new IllegalArgumentException("Invalid inward purpose"));
			inwardTradingEntity.setPurposeId(purpose.getPurposeId());
			inwardTradingEntity.setPurposeType(purpose.getPurposeName());
			return;
		}
		if (request.getPurposeType() != null && !request.getPurposeType().trim().isEmpty()) {
			String purposeType = request.getPurposeType().trim();
			Optional<InwardPurposeEntity> purpose = inwardPurposeRepository.findByPurposeNameIgnoreCaseAndIsActiveTrue(purposeType);
			if (purpose.isPresent()) {
				inwardTradingEntity.setPurposeId(purpose.get().getPurposeId());
				inwardTradingEntity.setPurposeType(purpose.get().getPurposeName());
			} else {
				// Preserve legacy clients and records whose purpose predates the master table.
				inwardTradingEntity.setPurposeId(null);
				inwardTradingEntity.setPurposeType(purposeType);
			}
			return;
		}
		throw new IllegalArgumentException("Inward purpose is required");
	}

}
