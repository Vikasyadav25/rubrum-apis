package com.steel.product.trading.repository;

import com.steel.product.trading.entity.SubCategoryEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategoryEntity, Integer> {

	@Query("select inw from SubCategoryEntity inw where inw.isDeleted is false and inw.subcategoryName = :subcategoryName and inw.categoryId = :categoryId and inw.subcategoryId not in :subcategoryId")
	List<SubCategoryEntity> findBySubCategoryNameforUpdate(@Param("subcategoryName") String subcategoryName, @Param("categoryId") Integer categoryId, @Param("subcategoryId") Integer subcategoryId);
	
	@Query("select inw from SubCategoryEntity inw where inw.isDeleted is false and inw.subcategoryName = :subcategoryName and inw.categoryId = :categoryId")
	List<SubCategoryEntity> findBySubCategoryNameforInsert(@Param("subcategoryName") String subcategoryName, @Param("categoryId") Integer categoryId);

	@Query(value = "SELECT sub.subcategory_id, sub.subcategory_name, sub.subcategory_hsn_code, sub.category_id, cat.category_name"
			+ " FROM trading_subcategory_master sub, trading_category_master cat \r\n"
			+ " where sub.is_deleted = 0 and case when :searchText is not null and LENGTH(:searchText) >0 then (sub.subcategory_name like %:searchText% or cat.category_name like %:searchText% ) else 1=1 end " 
			+ " and sub.category_id = cat.category_id order by sub.subcategory_id desc ",
	countQuery = "SELECT count(sub.subcategory_id) "
			+ " FROM trading_subcategory_master sub, trading_category_master cat \r\n"
			+ " where sub.is_deleted = 0 and case when :searchText is not null and LENGTH(:searchText) >0 then (sub.subcategory_name like %:searchText% or cat.category_name like %:searchText% ) else 1=1 end " 
			+ " and sub.category_id = cat.category_id",
	nativeQuery = true)
	Page<Object[]> findAllWithSearchText(@Param("searchText") String searchText, Pageable pageable);
	
	@Modifying
	@Transactional
	@Query("update SubCategoryEntity inw set inw.isDeleted = true, inw.updatedBy=:userId, inw.updatedOn=CURRENT_TIMESTAMP where inw.subcategoryId in :itemIds")
	void deleteData(@Param("itemIds") List<Integer> itemIds, @Param("userId") Integer userId);
	
	Optional<SubCategoryEntity> findBySubcategoryIdAndIsDeleted(Integer subcategoryId, Boolean isDeleted);

	@Query("select sub from SubCategoryEntity sub where sub.isDeleted is false and sub.categoryId in :categoryIds order by sub.subcategoryName asc")
	List<SubCategoryEntity> findActiveByCategoryIds(@Param("categoryIds") List<Integer> categoryIds);

}
