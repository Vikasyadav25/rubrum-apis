package com.steel.product.trading.repository;

import com.steel.product.trading.entity.InwardPurposeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InwardPurposeRepository extends JpaRepository<InwardPurposeEntity, Integer> {

	List<InwardPurposeEntity> findByIsActiveTrueOrderByDisplayOrderAsc();

	Optional<InwardPurposeEntity> findByPurposeIdAndIsActiveTrue(Integer purposeId);

	Optional<InwardPurposeEntity> findByPurposeNameIgnoreCaseAndIsActiveTrue(String purposeName);
}
