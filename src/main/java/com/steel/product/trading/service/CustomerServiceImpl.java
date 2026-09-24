package com.steel.product.trading.service;

import com.steel.product.trading.entity.CustomerEntity;
import com.steel.product.trading.repository.CustomerRepository;
import com.steel.product.trading.request.DeleteRequest;
import com.steel.product.trading.request.SearchRequest;
import com.steel.product.trading.request.CustomerRequest;

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
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository;

	@Autowired
	SharedContactAddressService sharedContactAddressService;
	
	@Override
	@Transactional
	public ResponseEntity<Object> save(CustomerRequest customerRequest) {
		log.info("In CustomerSave page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		String message="Customer details saved successfully..! ";
		try {
			CustomerEntity customerEntity = new CustomerEntity();
			BeanUtils.copyProperties(customerRequest, customerEntity, "additionalContacts", "additionalAddresses");
			customerEntity.setIsDeleted(false);
			if (customerEntity.getCustomerId() != null && customerEntity.getCustomerId() > 0) {
				CustomerEntity oldEntity = null;

				Optional<CustomerEntity> kk = customerRepository.findById(customerEntity.getCustomerId());
				if (kk.isPresent()) {
					oldEntity = kk.get();
				}
				if(oldEntity!=null && oldEntity.getCustomerId()>0) {
					
					List<CustomerEntity> testItemName = customerRepository.findByCustomerName( customerEntity.getCustomerName(), oldEntity.getCustomerId());
					if(testItemName!=null && testItemName.size()>0) {
						return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Customer Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
					}
					customerEntity.setUpdatedBy( customerRequest.getUserId());
					customerEntity.setUpdatedOn(new Date());
					customerEntity.setCreatedBy(oldEntity.getCreatedBy());
					customerEntity.setCreatedOn(oldEntity.getCreatedOn());
					message="Customer details updated successfully..! ";
				} else {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Please enter valid data\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			} else {
				List<CustomerEntity> testItemName = customerRepository.findByCustomerName(customerEntity.getCustomerName());
				if(testItemName!=null && testItemName.size()>0) {
					return new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Entered Customer Name already used\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
				}
				customerEntity.setCreatedBy(customerRequest.getUserId());
				customerEntity.setCreatedOn(new Date());
			}
			customerEntity = customerRepository.save(customerEntity);
			sharedContactAddressService.syncContacts(
					"CUSTOMER", customerEntity.getCustomerId(),
					customerRequest.getAdditionalContacts(), customerRequest.getUserId());
			sharedContactAddressService.syncAddresses(
					"CUSTOMER", customerEntity.getCustomerId(),
					customerRequest.getAdditionalAddresses(), customerRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \""+message+" \"}",	new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("Unable to save customer", e);
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	@Override
	public Page<CustomerEntity> getCustomerList(SearchRequest searchListPageRequest) {
		log.info("In getCustomerList page ");
		Pageable pageable = PageRequest.of((searchListPageRequest.getPageNo() - 1), searchListPageRequest.getPageSize(), Sort.by("customerId").descending());

		if (searchListPageRequest.getSearchText() != null && searchListPageRequest.getSearchText().length() > 0) {
			Page<CustomerEntity> pageResult = customerRepository.findAllWithSearchText(searchListPageRequest.getSearchText(), pageable);
			pageResult.forEach(this::populateAdditionalDetails);
			return pageResult;
		} else {
			Page<CustomerEntity> pageResult = customerRepository.findAll(pageable);
			pageResult.forEach(this::populateAdditionalDetails);
			return pageResult;
		}
	}

	@Override
	public CustomerEntity findByCustomerId(Integer id) {
		log.info("In findByCustomerId page ");
		Optional<CustomerEntity> kk = customerRepository.findByCustomerIdAndIsDeleted(id, false);
		CustomerEntity categoryEntity = null;
		if (kk.isPresent()) {
			categoryEntity = kk.get();
			populateAdditionalDetails(categoryEntity);
		}
		return categoryEntity;
	}

	@Override
	public ResponseEntity<Object> customerDelete(DeleteRequest deleteRequest) {
		log.info("In customerDelete page ");
		ResponseEntity<Object> response = null;
		HttpHeaders header = new HttpHeaders();
		header.set("Content-Type", "application/json");
		
		try {
			customerRepository.deleteData(deleteRequest.getIds(), deleteRequest.getUserId());
			response = new ResponseEntity<>("{\"status\": \"success\", \"message\": \"Selected Customer has been deleted successfully..! \"}", new HttpHeaders(), HttpStatus.OK);
		} catch (Exception e) {
			response = new ResponseEntity<>("{\"status\": \"fail\", \"message\": \"Error Occurred\"}", header, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	private void populateAdditionalDetails(CustomerEntity customer) {
		customer.setAdditionalContacts(
				sharedContactAddressService.getContacts("CUSTOMER", customer.getCustomerId()));
		customer.setAdditionalAddresses(
				sharedContactAddressService.getAddresses("CUSTOMER", customer.getCustomerId()));
	}
	
}
