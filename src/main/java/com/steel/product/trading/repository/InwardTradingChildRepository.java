package com.steel.product.trading.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.steel.product.trading.entity.InwardTradingChildEntity;

@Repository
public interface InwardTradingChildRepository extends JpaRepository<InwardTradingChildEntity, Integer> {

	@Modifying
	@Transactional
	@Query("update InwardTradingChildEntity inward set inward.isDeleted = true, inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP where inward.itemchildId in :itemchildIds")
	void deleteData(@Param("itemchildIds") List<Integer> itemchildIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update InwardTradingChildEntity item set item.isDeleted = true, item.updatedBy=:userId, item.updatedOn=CURRENT_TIMESTAMP where item.inwardId.inwardId in :inwardIds")
	void deleteByInwardIds(@Param("inwardIds") List<Integer> inwardIds, @Param("userId") Integer userId);
}
