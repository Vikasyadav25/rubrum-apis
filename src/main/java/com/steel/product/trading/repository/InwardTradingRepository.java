package com.steel.product.trading.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.steel.product.trading.entity.InwardTradingEntity;

@Repository
public interface InwardTradingRepository extends JpaRepository<InwardTradingEntity, Integer> {
	
	@Query(value = "SELECT inward.inward_id, inward.consignment_id"
			+ " FROM trading_inward inward, trading_vendor_master vendor \r\n"
			+ " where inward.is_deleted = 0"
			+ " and case when :searchText is not null and LENGTH(:searchText) >0 then (vendor.vendor_name like %:searchText% or inward.consignment_id like %:searchText% or inward.document_type like %:searchText% or inward.consignment_id like %:searchText%) else 1=1 end " 
			+ " and case when :status is not null and LENGTH(:status) > 0 then upper(inward.status) = upper(:status) else 1=1 end "
			+ " and inward.vendor_id = ifnull(:vendorId, inward.vendor_id) \r\n"
			+ " and inward.inward_id = ifnull(:inwardId, inward.inward_id) \r\n"
			+ " and inward.vendor_id=vendor.vendor_id order by inward.inward_id desc ",
	countQuery = "SELECT count(inward.inward_id) "
			+ " FROM trading_inward inward, trading_vendor_master vendor \r\n"
			+ " where inward.is_deleted = 0" 
			+ " and case when :searchText is not null and LENGTH(:searchText) >0 then (vendor.vendor_name like %:searchText% or inward.consignment_id like %:searchText% or inward.document_type like %:searchText% or inward.consignment_id like %:searchText%) else 1=1 end " 
			+ " and case when :status is not null and LENGTH(:status) > 0 then upper(inward.status) = upper(:status) else 1=1 end "
			+ " and inward.vendor_id = ifnull(:vendorId, inward.vendor_id) \r\n"
			+ " and inward.inward_id = ifnull(:inwardId, inward.inward_id) \r\n"
			+ " and inward.vendor_id=vendor.vendor_id",
	nativeQuery = true)
	Page<Object[]> findAllInwardsWithSearchText(@Param("inwardId") Integer inwardId, @Param("vendorId") Integer vendorId, @Param("searchText") String searchText, @Param("status") String status, Pageable pageable);
	
	@Query(value = "SELECT inward.inward_id, vendor_name, COALESCE(purpose.purpose_name, inward.purpose_type), inward.vendor_id, inward.transporter_name, inward.transporter_phone_no, "
			+ " inward.vendor_batch_no, inward.consignment_id, inward.location_id, inward.vehicle_no, inward.document_no, inward.document_type,"
			+ " inward.document_date, inward.eway_bill_no, inward.eway_bill_date, inward.value_of_goods, inward.extra_charges_option,"
			+ " inward.freight_charges, inward.insurance_amount, inward.loading_charges, inward.weightmen_charges, inward.cgst, inward.sgst, "
			+ " inward.igst, inward.total_inward_volume, inward.total_weight, inward.total_volume, "
			+ " child.itemchild_id, child.item_id, child.unit, child.unit_volume, child.net_weight, child.rate,child.volume,"
			+ " child.actual_noof_pieces, child.theoretical_weight, child.weight_variance, child.theoretical_noof_pieces, "
			+ " (select item_name from trading_material_master mat where mat.item_id =  child.item_id) as itemName, "
			+ " child.inward_item_id, inward.inward_no, inward.status, inward.currency_code, inward.reconciliation_remark,"
			+ " inward.deduct_freight, inward.raise_debit_note, vendor.vendor_nickname,"
			+ " (select loc.location_name from trading_location_master loc where loc.location_id = inward.location_id) as location_name,"
			+ " inward.created_on, inward.purpose_id"
			+ " FROM trading_inward inward"
			+ " INNER JOIN trading_vendor_master vendor ON inward.vendor_id=vendor.vendor_id"
			+ " INNER JOIN trading_inward_items child ON inward.inward_id=child.inwardid"
			+ " LEFT JOIN trading_inward_purpose_master purpose ON inward.purpose_id=purpose.purpose_id \r\n"
			+ " where inward.is_deleted = 0 and child.is_deleted = 0 " 
			+ " and inward.inward_id in :inwardIds \r\n"
			+ " order by inward.inward_id desc ",
	nativeQuery = true)
	List<Object[]> findAllInwardsWiseData(@Param("inwardIds") List<Integer> inwardIds);

	@Modifying
	@Transactional
	@Query("update InwardTradingEntity inward set inward.isDeleted = true, inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP where inward.inwardId in :inwardIds")
	void deleteData(@Param("inwardIds") List<Integer> inwardIds, @Param("userId") Integer userId);
}
