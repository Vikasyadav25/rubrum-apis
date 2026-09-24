package com.steel.product.trading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.steel.product.trading.entity.ContactMasterEntity;

@Repository
public interface ContactMasterRepository extends JpaRepository<ContactMasterEntity, Integer> {
	List<ContactMasterEntity> findByReferenceTypeAndReferenceIdAndIsDeletedFalseOrderByContactIdAsc(
			String referenceType, Integer referenceId);
}
