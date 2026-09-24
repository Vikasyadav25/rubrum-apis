package com.steel.product.trading.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.steel.product.trading.repository.DORepository;
import com.steel.product.trading.repository.DCRepository;
import com.steel.product.trading.request.*;
import com.steel.product.trading.service.DispatchHandoffService;

@RestController
@CrossOrigin
@RequestMapping("/trading")
public class DispatchHandoffController {
    private final DispatchHandoffService handoff;
    private final DORepository orders;
    private final DCRepository challans;

    public DispatchHandoffController(DispatchHandoffService handoff, DORepository orders, DCRepository challans) {
        this.handoff = handoff;
        this.orders = orders;
        this.challans = challans;
    }

    @PostMapping("/proforma/push-to-dispatch")
    public Map<String, Object> push(@RequestBody PushToDispatchRequest request) {
        return handoff.push(request);
    }

    @PostMapping("/dispatch/documents")
    public Map<String, Object> documents(@RequestBody PushToDispatchRequest request) {
        if (request.getEnquiryId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enquiry is required.");
        Map<String, Object> result = new LinkedHashMap<>();
        orders.findAllEnqIds(request.getEnquiryId()).stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).findFirst().ifPresent(entity -> {
            DeliveryOrderRequest dto = new DeliveryOrderRequest();
            BeanUtils.copyProperties(entity, dto, "enquiryId");
            dto.setEnquiryId(request.getEnquiryId());
            result.put("deliveryOrder", dto);
        });
        challans.findAllEnqIds(request.getEnquiryId()).stream().filter(d -> !Boolean.TRUE.equals(d.getIsDeleted())).findFirst().ifPresent(entity -> {
            DeliveryChalanRequest dto = new DeliveryChalanRequest();
            BeanUtils.copyProperties(entity, dto, "enquiryId");
            dto.setEnquiryId(request.getEnquiryId());
            result.put("deliveryChallan", dto);
        });
        return result;
    }

    @PostMapping("/do/approve-loading")
    public Map<String, Object> approveLoading(@RequestBody PushToDispatchRequest request) {
        return handoff.approveLoading(request);
    }
}
