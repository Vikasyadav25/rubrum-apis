package com.steel.product.trading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.steel.product.trading.entity.AddressMasterEntity;

@Repository
public interface AddressMasterRepository extends JpaRepository<AddressMasterEntity, Integer> {
	List<AddressMasterEntity> findByReferenceTypeAndReferenceIdAndIsDeletedFalseOrderByAddressIdAsc(
			String referenceType, Integer referenceId);
}
