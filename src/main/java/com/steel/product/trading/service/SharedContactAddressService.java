package com.steel.product.trading.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.steel.product.trading.entity.AddressMasterEntity;
import com.steel.product.trading.entity.ContactMasterEntity;
import com.steel.product.trading.repository.AddressMasterRepository;
import com.steel.product.trading.repository.ContactMasterRepository;
import com.steel.product.trading.request.AddressMasterRequest;
import com.steel.product.trading.request.ContactMasterRequest;

@Service
public class SharedContactAddressService {

	@Autowired
	private ContactMasterRepository contactRepository;

	@Autowired
	private AddressMasterRepository addressRepository;

	public void syncContacts(String referenceType, Integer referenceId,
			List<ContactMasterRequest> requests, Integer userId) {
		if (requests == null) {
			return;
		}
		List<ContactMasterEntity> existingContacts = getContacts(referenceType, referenceId);
		Map<Integer, ContactMasterEntity> existingById = new HashMap<>();
		for (ContactMasterEntity contact : existingContacts) {
			existingById.put(contact.getContactId(), contact);
		}
		Set<Integer> retainedIds = new HashSet<>();
		for (ContactMasterRequest request : requests) {
			if (!hasText(request.getContactName()) || !hasText(request.getPhoneNo())) {
				throw new IllegalArgumentException("Additional contact name and phone are required");
			}
			ContactMasterEntity contact;
			if (request.getContactId() != null) {
				contact = existingById.get(request.getContactId());
				if (contact == null) {
					throw new IllegalArgumentException("Invalid additional contact reference");
				}
				retainedIds.add(contact.getContactId());
				contact.setUpdatedBy(userId);
			} else {
				contact = new ContactMasterEntity();
				contact.setReferenceType(referenceType);
				contact.setReferenceId(referenceId);
				contact.setCreatedBy(userId);
			}
			contact.setContactName(request.getContactName().trim());
			contact.setPhoneNo(request.getPhoneNo().trim());
			contact.setAlternatePhoneNo(trimToNull(request.getAlternatePhoneNo()));
			contact.setEmailId(trimToNull(request.getEmailId()));
			contact.setIsDeleted(false);
			contactRepository.save(contact);
		}
		for (ContactMasterEntity contact : existingContacts) {
			if (!retainedIds.contains(contact.getContactId())) {
				contact.setIsDeleted(true);
				contact.setUpdatedBy(userId);
				contactRepository.save(contact);
			}
		}
	}

	public void syncAddresses(String referenceType, Integer referenceId,
			List<AddressMasterRequest> requests, Integer userId) {
		if (requests == null) {
			return;
		}
		List<AddressMasterEntity> existingAddresses = getAddresses(referenceType, referenceId);
		Map<Integer, AddressMasterEntity> existingById = new HashMap<>();
		for (AddressMasterEntity address : existingAddresses) {
			existingById.put(address.getAddressId(), address);
		}
		Set<Integer> retainedIds = new HashSet<>();
		for (AddressMasterRequest request : requests) {
			if (!hasText(request.getAddress1()) || !hasText(request.getCity())
					|| !hasText(request.getState()) || !hasText(request.getPincode())) {
				throw new IllegalArgumentException("Additional address, city, state and pincode are required");
			}
			AddressMasterEntity address;
			if (request.getAddressId() != null) {
				address = existingById.get(request.getAddressId());
				if (address == null) {
					throw new IllegalArgumentException("Invalid additional address reference");
				}
				retainedIds.add(address.getAddressId());
				address.setUpdatedBy(userId);
			} else {
				address = new AddressMasterEntity();
				address.setReferenceType(referenceType);
				address.setReferenceId(referenceId);
				address.setCreatedBy(userId);
			}
			address.setAddress1(request.getAddress1().trim());
			address.setAddress2(trimToNull(request.getAddress2()));
			address.setCity(request.getCity().trim());
			address.setState(request.getState().trim());
			address.setPincode(request.getPincode().trim());
			address.setIsDeleted(false);
			addressRepository.save(address);
		}
		for (AddressMasterEntity address : existingAddresses) {
			if (!retainedIds.contains(address.getAddressId())) {
				address.setIsDeleted(true);
				address.setUpdatedBy(userId);
				addressRepository.save(address);
			}
		}
	}

	public List<ContactMasterEntity> getContacts(String referenceType, Integer referenceId) {
		return contactRepository
				.findByReferenceTypeAndReferenceIdAndIsDeletedFalseOrderByContactIdAsc(referenceType, referenceId);
	}

	public List<AddressMasterEntity> getAddresses(String referenceType, Integer referenceId) {
		return addressRepository
				.findByReferenceTypeAndReferenceIdAndIsDeletedFalseOrderByAddressIdAsc(referenceType, referenceId);
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private String trimToNull(String value) {
		return hasText(value) ? value.trim() : null;
	}
}
