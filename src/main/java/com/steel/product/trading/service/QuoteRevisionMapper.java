package com.steel.product.trading.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeanUtils;
import com.steel.product.trading.entity.EQPTermsEntity;
import com.steel.product.trading.entity.QuoteRevisionEntity;
import com.steel.product.trading.entity.QuoteRevisionItemEntity;
import com.steel.product.trading.entity.QuoteRevisionTermsEntity;
import com.steel.product.trading.request.EQPChildRequest;
import com.steel.product.trading.request.EQPRequest;

/** Converts detached revision rows to the existing API snapshot contract. */
public final class QuoteRevisionMapper {
    private QuoteRevisionMapper() { }

    public static void store(EQPRequest request, QuoteRevisionEntity revision) {
        if (request.getItemsList() == null) {
            throw new IllegalArgumentException("Quotation revision items are required");
        }
        BeanUtils.copyProperties(request, revision, "enquiryId", "itemsList", "terms");
        revision.getItemsList().clear();
        int lineNo = 1;
        for (EQPChildRequest source : request.getItemsList()) {
            QuoteRevisionItemEntity item = new QuoteRevisionItemEntity();
            BeanUtils.copyProperties(source, item);
            item.setRevision(revision);
            item.setLineNo(lineNo++);
            revision.getItemsList().add(item);
        }
        if (request.getTerms() != null) {
            QuoteRevisionTermsEntity terms = new QuoteRevisionTermsEntity();
            BeanUtils.copyProperties(request.getTerms(), terms);
            terms.setRevision(revision);
            revision.setTerms(terms);
        } else {
            revision.setTerms(null);
        }
    }

    public static EQPRequest snapshot(QuoteRevisionEntity revision) {
        EQPRequest request = new EQPRequest();
        BeanUtils.copyProperties(revision, request, "itemsList", "terms");
        List<EQPChildRequest> items = new ArrayList<>();
        for (QuoteRevisionItemEntity source : revision.getItemsList()) {
            EQPChildRequest item = new EQPChildRequest();
            BeanUtils.copyProperties(source, item);
            items.add(item);
        }
        request.setItemsList(items);
        if (revision.getTerms() != null) {
            EQPTermsEntity terms = new EQPTermsEntity();
            BeanUtils.copyProperties(revision.getTerms(), terms);
            request.setTerms(terms);
        }
        return request;
    }
}
