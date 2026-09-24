package com.steel.product.trading.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.steel.product.trading.entity.CommercialTermMasterEntity;

@Repository
public interface CommercialTermMasterRepository extends JpaRepository<CommercialTermMasterEntity, Integer> {
    @Query("select term from CommercialTermMasterEntity term where term.isDeleted is false "
        + "and (:termType is null or term.termType = :termType) "
        + "and (:searchText is null or lower(term.termName) like lower(concat('%', :searchText, '%'))) "
        + "order by term.sortOrder asc, term.termName asc")
    List<CommercialTermMasterEntity> findActive(
        @Param("termType") String termType,
        @Param("searchText") String searchText);

    @Query("select term from CommercialTermMasterEntity term where term.isDeleted is false "
        + "and term.termType = :termType and lower(term.termName) = lower(:termName) "
        + "and (:termId is null or term.commercialTermId <> :termId)")
    List<CommercialTermMasterEntity> findDuplicate(
        @Param("termType") String termType,
        @Param("termName") String termName,
        @Param("termId") Integer termId);

    Optional<CommercialTermMasterEntity> findByCommercialTermIdAndIsDeleted(Integer id, Boolean isDeleted);

    @Modifying
    @Transactional
    @Query("update CommercialTermMasterEntity term set term.isDeleted = true, term.updatedBy = :userId, "
        + "term.updatedOn = CURRENT_TIMESTAMP where term.commercialTermId in :ids")
    void softDelete(@Param("ids") List<Integer> ids, @Param("userId") Integer userId);
}
