package com.steel.product.trading.service;

import com.steel.product.trading.dto.EQPChildResponse;
import com.steel.product.trading.dto.EQPResponse;
import com.steel.product.trading.entity.EQPChildEntity;
import com.steel.product.trading.entity.EQPEntity;
import com.steel.product.trading.entity.EQPTermsEntity;
import com.steel.product.trading.repository.EQPChildRepository;
import com.steel.product.trading.repository.EQPRepository;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.EQPChildRequest;
import com.steel.product.trading.request.EQPRequest;
import com.steel.product.trading.request.EQPSearchRequest;

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
public class EQPServiceImpl implements EQPService {

	@Autowired
	EQPRepository eqpRepository;

	@Autowired
	EQPChildRepository childRepository;

	@Autowired
	QuoteRevisionService quoteRevisionService;

	@Override
	public ResponseEntity<Object> save(EQPRequest request) {
		log.info("In EQPServiceImpl.save page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message = "Enquiry Details saved successfully..! ";
		try {
			EQPEntity eqpEntity = new EQPEntity();
			BeanUtils.copyProperties(request, eqpEntity);
			eqpEntity.setIsDeleted( false);
			eqpEntity.setEnqStatus( request.getStatus());

			Map<Integer, Integer> oldChildIdsMap = new HashMap<>();
			List<Integer> missedChildIds = new ArrayList<>();
			
			if (request.getEnquiryId() != null && request.getEnquiryId() > 0) {
				EQPEntity oldEntity = null;

				Optional<EQPEntity> kk = eqpRepository.findById(eqpEntity.getEnquiryId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if (oldEntity != null && oldEntity.getEnquiryId() > 0) {

					for (EQPChildEntity childEntity : oldEntity.getItemsList()) {
						oldChildIdsMap.put(childEntity.getEnquiryChildId(), childEntity.getEnquiryChildId());
					}
					eqpEntity.setItemsList(oldEntity.getItemsList());
					eqpEntity.setUpdatedBy(request.getUserId());
					eqpEntity.setUpdatedOn(new Date());
					eqpEntity.setCreatedBy(oldEntity.getCreatedBy());
					eqpEntity.setCreatedOn(oldEntity.getCreatedOn());
					eqpEntity.setCurrentStatus(request.getStatus());
					message = "Enquiry Details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid enquiry data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}

				// ENQUIRY UPDATE LOGIC
				eqpRepository.save(eqpEntity);
				List<EQPChildEntity> itemsList = new ArrayList<>();
				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = new EQPChildEntity();
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setUpdatedBy( request.getUserId());
					childEntity.setUpdatedOn(new Date());
					childEntity.setCreatedBy(request.getUserId());
					childEntity.setCreatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					itemsList.add(childEntity);
					if (childReq.getEnquiryChildId() != null && childReq.getEnquiryChildId() > 0) {
						oldChildIdsMap.remove(childReq.getEnquiryChildId());
					}
				}

				if (oldChildIdsMap != null && oldChildIdsMap.size() > 0) {
					missedChildIds = new ArrayList<Integer>(oldChildIdsMap.values());
				}
				childRepository.saveAll(itemsList);
				childRepository.deleteData(missedChildIds, request.getUserId());
			} else {
				eqpEntity.setQuoteCreatedBy(request.getUserId());
				eqpEntity.setQuoteCreatedOn(new Date());
				eqpEntity.setCurrentStatus(request.getStatus());
				
				System.out.println("request.getItemsList.size ==  " + request.getItemsList().size());
				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = new EQPChildEntity();
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setCreatedBy(request.getUserId());
					childEntity.setCreatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					eqpEntity.addItem(childEntity);
				}
				System.out.println("eqpEntity.getItemsList ==  " + eqpEntity.getItemsList().size());
				eqpRepository.save(eqpEntity);
			}
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message
					+" \", \"enquiryId\": "+eqpEntity.getEnquiryId()+"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Map<String, Object> getEQPList(EQPSearchRequest searchPageRequest) {
		log.info("In getEQPList page ");
		Map<String, Object> response = new HashMap<>();
		Pageable pageable = PageRequest.of((searchPageRequest.getPageNo() - 1), searchPageRequest.getPageSize());
		
		if(searchPageRequest.getStatus() !=null && searchPageRequest.getStatus().length()==0) {
			searchPageRequest.setStatus( null);
		}
		if(searchPageRequest.getSearchText() !=null && searchPageRequest.getSearchText().length()==0) {
			searchPageRequest.setSearchText( null);
		}
		Page<Object[]> pageResult = eqpRepository.findAllInwardsWithSearchText(searchPageRequest.getEnquiryId(),
				searchPageRequest.getStatus(), searchPageRequest.getCustomerId(), searchPageRequest.getSearchText(),
				Boolean.TRUE.equals(searchPageRequest.getDispatchOnly()), pageable);
		List<Integer> inwardIdsList = new ArrayList<>();
		for (Object[] result : pageResult) {
			inwardIdsList.add(result[0] != null ? (Integer) result[0] : null);
			System.out.println("result == "+result[0] != null ? (Integer) result[0] : null);
		}
		System.out.println("getEQPList == "+inwardIdsList.size());

		Map<Integer, List<EQPChildResponse>> map = new LinkedHashMap<>();
		Map<Integer, EQPResponse> inwardMap = new LinkedHashMap<>();
		List<EQPResponse> responseList = new ArrayList<>();
		List<Object[]> pageResultChild = inwardIdsList.isEmpty() ? new ArrayList<>() : eqpRepository.findAllInwardsWiseData(inwardIdsList, searchPageRequest.getStatus());
		for (Object[] result : pageResultChild) {
			EQPResponse dto = new EQPResponse();
			EQPChildResponse childDTO = new EQPChildResponse();
			dto.setEnquiryId( result[0] != null ? (Integer) result[0] : null);
			dto.setEnqCustomerId( result[1] != null ? (Integer) result[1] : null);
			dto.setEnqCustomerName( result[2] != null ? (String) result[2] : null);
			dto.setEnqEnquiryFrom( result[3] != null ? (String) result[3] : null);
			dto.setEnqEnquiryDate( result[4] != null ? (String) result[4] : null);
			dto.setEnqQty( result[5] != null ? (BigDecimal) result[5] : null);
			dto.setEnqValue( result[6] != null ? (BigDecimal) result[6] : null);
			dto.setQuoteEnquiryFrom( result[7] != null ? (String) result[7] : null);
			dto.setQuoteEnquiryDate( result[8] != null ? (String) result[8] : null);
			dto.setQuoteQty( result[9] != null ? (BigDecimal) result[9] : null);
			dto.setQuoteValue( result[10] != null ? (BigDecimal) result[10] : null);
			dto.setStatus( result[11] != null ? (String) result[11] : null);
			dto.setQuoteCustomerName( result[12] != null ? (String) result[12] : null);
			 
			// child items
			childDTO.setEnquiryChildId( result[13] != null ? (Integer) result[13] : null);
			childDTO.setItemId(result[14] != null ? (Integer) result[14] : null);
			childDTO.setItemSpecs( result[15] != null ? (String) result[15] : null);
			childDTO.setMake( result[16] != null ? (String) result[16] : null);
			childDTO.setAltMake( result[17] != null ? (String) result[17] : null);
			childDTO.setQty1( result[18] != null ? (BigDecimal) result[18] : null);
			childDTO.setUnit1( result[19] != null ? (String) result[19] : null);
			childDTO.setQty2(result[20] != null ? (BigDecimal) result[20] : null);
			childDTO.setUnit2(result[21] != null ? (String) result[21] : null);
			childDTO.setEstimateDeliveryDate( result[22] != null ? (String) result[22] : null);
			childDTO.setRemarks( result[23] != null ? (String) result[23] : null);
			childDTO.setStatus( result[24] != null ? (String) result[24] : null);
			childDTO.setLocationId( result[25] != null ? (Integer) result[25] : null);
			childDTO.setItemName( result[26] != null ? (String) result[26] : null);
			childDTO.setLocationName( result[27] != null ? (String) result[27] : null);

			dto.getTerms().setTermsId( result[28] != null ? (Integer) result[28] : null);
			dto.getTerms().setPaymentMethod(result[29] != null ? (String) result[29] : null);
			dto.getTerms().setWeight(result[30] != null ? (String) result[30] : null);
			dto.getTerms().setLoading(result[31] != null ? (String) result[31] : null);
			dto.getTerms().setTransportMethod(result[32] != null ? (String) result[32] : null);
			dto.getTerms().setOtherChargesMethod(result[33] != null ? (String) result[33] : null);
			dto.getTerms().setTaxMethod(result[34] != null ? (String) result[34] : null);
			dto.getTerms().setValidity(result[35] != null ? (String) result[35] : null);
			dto.getTerms().setRemarks(result[36] != null ? (String) result[36] : null);
			dto.getTerms().setTaxableAmount(result[37] != null ? (BigDecimal) result[37] : null);
			dto.getTerms().setLoadinge200PerTon(result[38] != null ? (BigDecimal) result[38] : null);
			dto.getTerms().setTransportCharges(result[39] != null ? (BigDecimal) result[39] : null);
			dto.getTerms().setOtherCharges(result[40] != null ? (BigDecimal) result[40] : null);
			dto.getTerms().setTotalTaxableAmount(result[41] != null ? (BigDecimal) result[41] : null);
			dto.getTerms().setGst(result[42] != null ? (BigDecimal) result[42] : null);
			dto.getTerms().setTotalEstimate(result[43] != null ? (BigDecimal) result[43] : null);
			dto.getTerms().setRAndO( result[44] != null ? (BigDecimal) result[44] : null);
			dto.getTerms().setLoadingQty(result[45] != null ? (BigDecimal) result[45] : null);
			dto.getTerms().setLoadingRate(result[46] != null ? (BigDecimal) result[46] : null);
			dto.getTerms().setTransportQty(result[47] != null ? (BigDecimal) result[47] : null);
			dto.getTerms().setTransportRate(result[48] != null ? (BigDecimal) result[48] : null);
			childDTO.setRate(result[49] != null ? (BigDecimal) result[49] : null);
			childDTO.setChargeableUnit(result[50] != null ? (String) result[50] : null);
			childDTO.setAmount(result[51] != null ? (BigDecimal) result[51] : null);
			dto.setLatestRevisionStatus(result[52] != null ? (String) result[52] : null);
			dto.setProformaStatus(result[53] != null ? (String) result[53] : null);
			dto.setQuoteCustomerId(result[54] != null ? ((Number) result[54]).intValue() : null);
			dto.setDeliveryOrderStatus(result[55] != null ? (String) result[55] : null);
			
			if (map.get(dto.getEnquiryId() ) != null) {
				List<EQPChildResponse> dummyList = map.get(dto.getEnquiryId());
				dummyList.add(childDTO);
				map.put(dto.getEnquiryId(), dummyList);
			} else {
				List<EQPChildResponse> dummyList = new ArrayList<>();
				dummyList.add(childDTO);
				map.put(dto.getEnquiryId(), dummyList);
			}
			inwardMap.put(dto.getEnquiryId(), dto);
		}
		for (Entry<Integer, List<EQPChildResponse>> entry : map.entrySet()) {
			EQPResponse dummyEntity = inwardMap.get(entry.getKey());
			dummyEntity.setItemsList(entry.getValue());
			responseList.add(dummyEntity);
		}

		response.put("content", responseList);
		response.put("currentPage", pageResult.getNumber());
		response.put("totalItems", pageResult.getTotalElements());
		response.put("totalPages", pageResult.getTotalPages());

		return response;
	}
 
	@Override
	@Transactional
	public ResponseEntity<Object> quoteSave(EQPRequest request) {
		log.info("In EQPServiceImpl.save page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message = "Quote Details saved successfully..! ";
		try {
			EQPEntity eqpEntity = new EQPEntity();
			Optional<EQPEntity> kk = eqpRepository.findById(request.getEnquiryId());
			EQPEntity oldEntity = null;
			if (kk.isPresent()) {
				oldEntity = kk.get();
				eqpEntity = kk.get();
			} else {
				return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid enquiry data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			boolean isQuoteUpdate = "QUOTE".equals(oldEntity.getCurrentStatus());
			
			eqpEntity.setQuoteCustomerId(request.getQuoteCustomerId() );
			eqpEntity.setQuoteEnquiryFrom(request.getQuoteEnquiryFrom() );
			eqpEntity.setQuoteEnquiryDate(request.getQuoteEnquiryDate());
			eqpEntity.setQuoteQty(request.getQuoteQty() );
			eqpEntity.setQuoteValue(request.getQuoteValue() );
			eqpEntity.setIsDeleted( false);
			eqpEntity.setQuoteStatus( request.getStatus());

			if (oldEntity.getEnquiryId() != null && oldEntity.getEnquiryId() > 0 && "QUOTE".equals(oldEntity.getCurrentStatus())) {
				Map<Integer, EQPChildEntity> existingChildren = new HashMap<>();
				for (EQPChildEntity childEntity : oldEntity.getItemsList()) {
					if("QUOTE".equals(childEntity.getStatus())) {
						existingChildren.put(childEntity.getEnquiryChildId(), childEntity);
					}
				}
				eqpEntity.setQuoteUpdatedBy(request.getUserId());
				eqpEntity.setQuoteUpdatedOn(new Date());
				eqpEntity.setCurrentStatus(request.getStatus());
				message = "Quote Details updated successfully..! ";

				EQPTermsEntity termsEntity = eqpEntity.getTerms();
				if (termsEntity == null) {
					termsEntity = new EQPTermsEntity();
				}
				BeanUtils.copyProperties(request.getTerms(), termsEntity, "termsId", "enquiryId");
				termsEntity.setIsDeleted(false);
				termsEntity.setEnquiryId(eqpEntity);
				termsEntity.setQuoteUpdatedBy(request.getUserId());
				termsEntity.setQuoteUpdatedOn(new Date());
				termsEntity.setStatus(request.getStatus());
				eqpEntity.setTerms(termsEntity);

				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = childReq.getEnquiryChildId() == null
							? null
							: existingChildren.remove(childReq.getEnquiryChildId());
					boolean isNewChild = childEntity == null;
					if (isNewChild) {
						childEntity = new EQPChildEntity();
					}
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setQuoteUpdatedBy(request.getUserId());
					childEntity.setQuoteUpdatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					if (isNewChild) {
						childEntity.setQuoteCreatedBy(request.getUserId());
						childEntity.setQuoteCreatedOn(new Date());
						eqpEntity.addItem(childEntity);
					}
				}

				for (EQPChildEntity omittedChild : existingChildren.values()) {
					omittedChild.setIsDeleted(true);
					omittedChild.setUpdatedBy(request.getUserId());
					omittedChild.setUpdatedOn(new Date());
				}
				eqpRepository.save(eqpEntity);
			} else {
				eqpEntity.setQuoteCreatedBy(request.getUserId());
				eqpEntity.setQuoteCreatedOn(new Date());
				eqpEntity.setCurrentStatus(request.getStatus());

				Map<Integer, EQPChildEntity> existingChildren = new HashMap<>();
				for (EQPChildEntity existingChild : eqpEntity.getItemsList()) {
					if (existingChild.getEnquiryChildId() != null) {
						existingChildren.put(existingChild.getEnquiryChildId(), existingChild);
					}
				}

				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = childReq.getEnquiryChildId() == null
							? null
							: existingChildren.remove(childReq.getEnquiryChildId());
					boolean isNewChild = childEntity == null;
					if (isNewChild) {
						childEntity = new EQPChildEntity();
					}
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setQuoteCreatedBy(request.getUserId());
					childEntity.setQuoteCreatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					if (isNewChild) {
						eqpEntity.addItem(childEntity);
					}
				}

				for (EQPChildEntity omittedChild : existingChildren.values()) {
					omittedChild.setIsDeleted(true);
					omittedChild.setUpdatedBy(request.getUserId());
					omittedChild.setUpdatedOn(new Date());
				}
				EQPTermsEntity termsEntity = request.getTerms();
				termsEntity.setIsDeleted(false);
				termsEntity.setEnquiryId(eqpEntity);
				termsEntity.setQuoteCreatedBy(request.getUserId());
				termsEntity.setQuoteCreatedOn(new Date());
				termsEntity.setStatus(request.getStatus());
				eqpEntity.setTerms( termsEntity);
				eqpRepository.save(eqpEntity);
			}
			quoteRevisionService.captureRevision(request, isQuoteUpdate);
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	@Override
	public ResponseEntity<Object> enquiryDelete(DeleteRequest deleteRequest) {
		log.info("In enquiryDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			eqpRepository.deleteEnquiryMainData(deleteRequest.getIds(), deleteRequest.getUserId());
			childRepository.deleteEnquiryChildData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Enquiry details has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	@Override
	public ResponseEntity<Object> quoteDelete(DeleteRequest deleteRequest) {
		log.info("In quoteDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			eqpRepository.deleteQuoteMainData(deleteRequest.getIds(), deleteRequest.getUserId());
			childRepository.deleteQuoteChildData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Quote details has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	@Override
	public ResponseEntity<Object> proformaSave(EQPRequest request) {
		log.info("In EQPServiceImpl.save page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message = "Proforma Details saved successfully..! ";
		try {
			EQPEntity eqpEntity = new EQPEntity();
			Map<Integer, Integer> oldChildIdsMap = new HashMap<>();
			List<Integer> missedChildIds = new ArrayList<>();
			Optional<EQPEntity> kk = eqpRepository.findById(request.getEnquiryId());
			EQPEntity oldEntity = null;
			if (kk.isPresent()) {
				oldEntity = kk.get();
				eqpEntity = kk.get();
			} else {
				return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
			}
			
			eqpEntity.setQuoteCustomerId(request.getQuoteCustomerId() );
			eqpEntity.setQuoteEnquiryFrom(request.getQuoteEnquiryFrom() );
			eqpEntity.setQuoteEnquiryDate(request.getQuoteEnquiryDate());
			eqpEntity.setQuoteQty(request.getQuoteQty() );
			eqpEntity.setQuoteValue(request.getQuoteValue() );
			eqpEntity.setIsDeleted( false);
			eqpEntity.setProformaStatus( request.getStatus());

			if (oldEntity.getEnquiryId() != null && oldEntity.getEnquiryId() > 0 && "PROFORMA".equals(oldEntity.getCurrentStatus())) {
				for (EQPChildEntity childEntity : oldEntity.getItemsList()) {
					if("PROFORMA".equals(childEntity.getStatus())) {
						oldChildIdsMap.put(childEntity.getEnquiryChildId(), childEntity.getEnquiryChildId());
					}
				}
				eqpEntity.setItemsList(oldEntity.getItemsList());
				eqpEntity.setProformaUpdatedBy(request.getUserId());
				eqpEntity.setProformaUpdatedOn(new Date());
				//eqpEntity.setQuoteCreatedBy(oldEntity.getCreatedBy());
				//eqpEntity.setQuoteCreatedOn(oldEntity.getCreatedOn());
				eqpEntity.setCurrentStatus(request.getStatus());
				message = "Proforma Details updated successfully..! ";

				EQPTermsEntity termsEntity = request.getTerms();
				termsEntity.setTermsId(eqpEntity.getTerms().getTermsId() );
				termsEntity.setIsDeleted(false);
				termsEntity.setEnquiryId(eqpEntity);
				//termsEntity.setQuoteCreatedBy(request.getUserId());
				termsEntity.setProformaUpdatedBy(request.getUserId());
				termsEntity.setProformaUpdatedOn(new Date());
				//termsEntity.setQuoteCreatedOn(new Date());
				termsEntity.setStatus(request.getStatus());
				eqpEntity.setTerms(termsEntity);

				// QUOTE UPDATE LOGIC
				eqpRepository.save(eqpEntity);
				List<EQPChildEntity> itemsList = new ArrayList<>();
				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = new EQPChildEntity();
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setProformaUpdatedBy(request.getUserId());
					childEntity.setProformaUpdatedOn(new Date());
					childEntity.setProformaCreatedBy(request.getUserId());
					childEntity.setProformaCreatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					itemsList.add(childEntity);
					if (childReq.getEnquiryChildId() != null && childReq.getEnquiryChildId() > 0) {
						oldChildIdsMap.remove(childReq.getEnquiryChildId());
					}
				}

				if (oldChildIdsMap != null && oldChildIdsMap.size() > 0) {
					missedChildIds = new ArrayList<Integer>(oldChildIdsMap.values());
				}
				childRepository.saveAll(itemsList);
				childRepository.deleteData(missedChildIds, request.getUserId());
			} else {
				eqpEntity.setProformaCreatedBy(request.getUserId());
				eqpEntity.setProformaCreatedOn(new Date());
				eqpEntity.setCurrentStatus(request.getStatus());

				Map<Integer, EQPChildEntity> existingChildren = new HashMap<>();
				for (EQPChildEntity existingChild : eqpEntity.getItemsList()) {
					if (existingChild.getEnquiryChildId() != null) {
						existingChildren.put(existingChild.getEnquiryChildId(), existingChild);
					}
				}

				for (EQPChildRequest childReq : request.getItemsList()) {
					EQPChildEntity childEntity = childReq.getEnquiryChildId() == null
							? null
							: existingChildren.remove(childReq.getEnquiryChildId());
					boolean isNewChild = childEntity == null;
					if (isNewChild) {
						childEntity = new EQPChildEntity();
					}
					BeanUtils.copyProperties(childReq, childEntity);
					childEntity.setIsDeleted(false);
					childEntity.setEnquiryId(eqpEntity);
					childEntity.setProformaCreatedBy(request.getUserId());
					childEntity.setProformaCreatedOn(new Date());
					childEntity.setStatus(request.getStatus());
					if (isNewChild) {
						eqpEntity.addItem(childEntity);
					}
				}

				for (EQPChildEntity omittedChild : existingChildren.values()) {
					omittedChild.setIsDeleted(true);
					omittedChild.setUpdatedBy(request.getUserId());
					omittedChild.setUpdatedOn(new Date());
				}

				EQPTermsEntity termsEntity = eqpEntity.getTerms();
				if (termsEntity == null) {
					termsEntity = new EQPTermsEntity();
				}
				BeanUtils.copyProperties(request.getTerms(), termsEntity, "termsId", "enquiryId");
				termsEntity.setIsDeleted(false);
				termsEntity.setEnquiryId(eqpEntity);
				termsEntity.setProformaCreatedBy(request.getUserId());
				termsEntity.setProformaCreatedOn(new Date());
				termsEntity.setStatus(request.getStatus());
				eqpEntity.setTerms(termsEntity);
				eqpRepository.save(eqpEntity);
			}
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.info("error is ==" + e.getMessage());
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public ResponseEntity<Object> proformaDelete(DeleteRequest deleteRequest) {
		log.info("In proformaDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			eqpRepository.deleteProformaMainData(deleteRequest.getIds(), deleteRequest.getUserId());
			childRepository.deleteProformaChildData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected proforma details has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
}
