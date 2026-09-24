package com.steel.product.trading.service;

import com.steel.product.trading.entity.VendorEntity;
import com.steel.product.trading.repository.VendorRepository;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.SearchRequest;
import com.steel.product.trading.request.VendorRequest;

import lombok.extern.log4j.Log4j2;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
@Log4j2
public class VendorServiceImpl implements VendorService {

	@Autowired
	VendorRepository vendorRepository;

	@Autowired
	SharedContactAddressService sharedContactAddressService;
	
	@Override
	@Transactional
	public ResponseEntity<Object> save(VendorRequest vendorRequest) {
		log.info("In vendorsave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Vendor details saved successfully..! ";
		try {
			VendorEntity vendorEntity = new VendorEntity();
			BeanUtils.copyProperties(vendorRequest, vendorEntity, "additionalContacts", "additionalAddresses");
			vendorEntity.setIsDeleted(false);
			if (vendorEntity.getVendorId() != null && vendorEntity.getVendorId() > 0) {
				VendorEntity oldEntity = null;

				Optional<VendorEntity> kk = vendorRepository.findById(vendorEntity.getVendorId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getVendorId()>0) {
					
					List<VendorEntity> testItemName = vendorRepository.findByVendorName( vendorEntity.getVendorName(), oldEntity.getVendorId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Vendor Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					vendorEntity.setUpdatedBy( vendorRequest.getUserId());
					vendorEntity.setUpdatedOn(new Date());
					vendorEntity.setCreatedBy(oldEntity.getCreatedBy());
					vendorEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Vendor details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<VendorEntity> testItemName = vendorRepository.findByVendorName(vendorEntity.getVendorName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Vendor Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				vendorEntity.setCreatedBy(vendorRequest.getUserId());
				vendorEntity.setCreatedOn(new Date());
			}
			vendorEntity = vendorRepository.save(vendorEntity);
			sharedContactAddressService.syncContacts(
					"VENDOR", vendorEntity.getVendorId(),
					vendorRequest.getAdditionalContacts(), vendorRequest.getUserId());
			sharedContactAddressService.syncAddresses(
					"VENDOR", vendorEntity.getVendorId(),
					vendorRequest.getAdditionalAddresses(), vendorRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("Unable to save vendor", e);
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<VendorEntity> getVendorList(SearchRequest searchListPageRequest) {
		log.info("In getVendorList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("vendorId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<VendorEntity> pageResult = vendorRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			pageResult.forEach(this::populateAdditionalDetails);
			return pageResult;
		} else {
			Page<VendorEntity> pageResult = vendorRepository.findAll(pageable);
			pageResult.forEach(this::populateAdditionalDetails);
			return pageResult;
		}
	}

	@Override
	public VendorEntity findByVendorId(Integer id) {
		log.info("In findByVendorId page ");
		Optional<VendorEntity> kk = vendorRepository.findByVendorIdAndIsDeleted(id, false);
		VendorEntity categoryEntity = null;
		if (kk.isPresent()) {
			categoryEntity = kk.get();
			populateAdditionalDetails(categoryEntity);
		}
		return categoryEntity;
	}

	@Override
	public ResponseEntity<Object> vendorDelete(DeleteRequest deleteRequest) {
		log.info("In vendorDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			vendorRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected vendor has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private void populateAdditionalDetails(VendorEntity vendor) {
		vendor.setAdditionalContacts(sharedContactAddressService.getContacts("VENDOR", vendor.getVendorId()));
		vendor.setAdditionalAddresses(sharedContactAddressService.getAddresses("VENDOR", vendor.getVendorId()));
	}
	
}
