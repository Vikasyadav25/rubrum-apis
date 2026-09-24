package com.steel.product.trading.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import com.steel.product.trading.entity.QuoteRevisionEntity;

@Repository
public interface QuoteRevisionRepository extends JpaRepository<QuoteRevisionEntity, Integer> {

    @EntityGraph(attributePaths = {"itemsList", "terms"})
    List<QuoteRevisionEntity> findByEnquiryIdOrderByVersionNoDesc(Integer enquiryId);

    Optional<QuoteRevisionEntity> findTopByEnquiryIdOrderByVersionNoDesc(Integer enquiryId);

    Optional<QuoteRevisionEntity> findByRevisionIdAndEnquiryId(Integer revisionId, Integer enquiryId);
}
