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
import com.steel.product.trading.entity.EQPEntity;

@Repository
public interface EQPRepository extends JpaRepository<EQPEntity, Integer> {
	@org.springframework.data.jpa.repository.Lock(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from EQPEntity e where e.enquiryId = :enquiryId")
	java.util.Optional<EQPEntity> findForDispatchUpdate(@Param("enquiryId") Integer enquiryId);
	
	@Query(value = "SELECT inward.enquiry_id, inward.enq_qty"
			+ " FROM trading_eqp inward, trading_customer_master customer \r\n"
			+ " where inward.is_deleted = 0"
			+ " and (:dispatchOnly = false or inward.current_status in ('DO','DC') or inward.proforma_status = 'PENDING_DO') "
			+ " and inward.current_status = ifnull(:status, inward.current_status) "
			+ " and case when :searchText is not null and LENGTH(:searchText) >0 then (customer.customer_name like %:searchText% or inward.enq_enquiry_from like %:searchText%) else 1=1 end " 
			+ " and inward.enq_customer_id = ifnull(:customerId, inward.enq_customer_id) \r\n"
			+ " and inward.enquiry_id = ifnull(:enquiryId, inward.enquiry_id) \r\n"
			+ " and inward.enq_customer_id=customer.customer_id order by inward.enquiry_id desc ",
	countQuery = "SELECT count(inward.enquiry_id) "
			+ " FROM trading_eqp inward, trading_customer_master customer \r\n"
			+ " where inward.is_deleted = 0" 
			+ " and (:dispatchOnly = false or inward.current_status in ('DO','DC') or inward.proforma_status = 'PENDING_DO') "
			+ " and inward.current_status = ifnull(:status, inward.current_status) "
			+ " and case when :searchText is not null and LENGTH(:searchText) >0 then (customer.customer_name like %:searchText% or inward.enq_enquiry_from like %:searchText%) else 1=1 end " 
			+ " and inward.enq_customer_id = ifnull(:customerId, inward.enq_customer_id) \r\n"
			+ " and inward.enquiry_id = ifnull(:enquiryId, inward.enquiry_id) \r\n"
			+ " and inward.enq_customer_id=customer.customer_id",
	nativeQuery = true)
	Page<Object[]> findAllInwardsWithSearchText(@Param("enquiryId") Integer enquiryId, @Param("status") String status, 
			@Param("customerId") Integer customerId, @Param("searchText") String searchText, @Param("dispatchOnly") boolean dispatchOnly, Pageable pageable);
	
	@Query(value = "SELECT inward.enquiry_id, inward.enq_customer_id, customer.customer_name, inward.enq_enquiry_from, DATE_FORMAT(inward.enq_enquiry_date, '%d-%m-%y') enqenquirydate, "
			+ " inward.enq_qty, inward.enq_value, inward.quote_enquiry_from,  "
			+ " DATE_FORMAT(inward.quote_enquiry_date, '%d-%m-%y') , inward.quote_qty, inward.quote_value, inward.current_status,  "
			+ " (select cust.customer_name from trading_customer_master cust where cust.customer_id =  inward.quote_customer_id) as quoteCustomer_name, "
			+ " child.enquiry_child_id, child.item_id, child.item_specs, child.make, child.alt_make, child.qty1,child.unit1,"
			+ " child.qty2, child.unit2, child.estimate_delivery_date, child.remarks itemremarks, child.status chldstts, child.location_id,  "
			+ " (select item_name from trading_material_master mat where mat.item_id =  child.item_id) as itemName,"
			+ " (select location_name from trading_location_master location where location.location_id =  child.location_id) ,"
			+ " terms.terms_id, terms.payment_method, terms.weight, terms.loading, terms.transport_method, terms.other_charges_method, "
			+ " terms.tax_method, terms.validity, terms.remarks, terms.taxable_amount, terms.loadinge200_per_ton, terms.transport_charges, "
			+ " terms.other_charges, terms.total_taxable_amount, terms.gst, terms.total_estimate, terms.r_o, "
			+ " terms.loading_qty, terms.loading_rate, terms.transport_qty, terms.transport_rate, "
			+ " child.rate, child.chargeable_unit, child.amount, "
			+ " (select revision.revision_status from trading_quote_revision revision "
			+ " where revision.enquiry_id = inward.enquiry_id order by revision.version_no desc limit 1) latestRevisionStatus, inward.proforma_status, inward.quote_customer_id, inward.do_status "
			+ " FROM trading_eqp inward "
			+ " left outer join trading_customer_master customer on inward.enq_customer_id=customer.customer_id"
			+ " left outer join trading_eqp_items child on inward.enquiry_id=child.enquiryid and inward.current_status =  child.status and child.is_deleted = 0 \r\n"
			+ " left outer join trading_eqp_quote_terms terms on inward.enquiry_id=terms.enquiryid "
			+ " where inward.is_deleted = 0 " 
			//+ " and inward.status =  child.status \r\n"
			+ " and inward.current_status = ifnull(:status, inward.current_status) and child.status = ifnull(:status, child.status) \r\n"
			+ " and inward.enquiry_id in :enquiryIds \r\n"
			//+ " and inward.enq_customer_id=customer.customer_id "
			//+ " and inward.enquiry_id=child.enquiryid "
			+ " order by inward.enquiry_id desc ",
	nativeQuery = true)
	List<Object[]> findAllInwardsWiseData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("status") String status);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.isDeleted = true, inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP where inward.enquiryId in :enquiryIds and inward.currentStatus='ENQUIRY'")
	void deleteEnquiryMainData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.quoteCustomerId=null, inward.quoteEnquiryFrom=null, inward.quoteEnquiryDate=null, inward.quoteQty=null, inward.quoteValue=null, inward.quoteCreatedBy=null, inward.quoteUpdatedBy=null, inward.quoteCreatedOn=null, inward.quoteUpdatedOn=null, inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, status='ENQUIRY' where inward.enquiryId in :enquiryIds and inward.currentStatus='QUOTE'")
	void deleteQuoteMainData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.proformaCreatedBy=null, inward.proformaUpdatedBy=null, inward.proformaCreatedOn=null, inward.proformaUpdatedOn=null, inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, status='QUOTE' where inward.enquiryId in :enquiryIds and inward.currentStatus='PROFORMA'")
	void deleteProformaMainData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.currentStatus='DO', inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, dOStatus='DO' where inward.enquiryId in :enquiryIds and inward.currentStatus='PROFORMA'")
	void updateDOStatus(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, currentStatus='PROFORMA' where inward.enquiryId in :enquiryIds and inward.currentStatus='DO'")
	void deleteDOMainData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.currentStatus='DC', inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, dOStatus='DO' where inward.enquiryId in :enquiryIds and inward.currentStatus='DO'")
	void updateDCStatus(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

	@Modifying
	@Transactional
	@Query("update EQPEntity inward set inward.updatedBy=:userId, inward.updatedOn=CURRENT_TIMESTAMP, currentStatus='DO' where inward.enquiryId in :enquiryIds and inward.currentStatus='DC'")
	void deleteDCMainData(@Param("enquiryIds") List<Integer> enquiryIds, @Param("userId") Integer userId);

}
