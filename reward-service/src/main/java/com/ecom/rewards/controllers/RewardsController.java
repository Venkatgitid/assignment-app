package com.ecom.rewards.controllers;

import com.ecom.rewards.dto.CustomerTransactionDto;
import com.ecom.rewards.dto.RewardsReqDto;
import com.ecom.rewards.services.RewardsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.ecom.rewards.utils.ControllerUtils.getErrorResponseEntity;


@Slf4j
@RestController
@RequestMapping("/rewards")
public class RewardsController {

    @Autowired
    private RewardsService rewardsService;

    @GetMapping("/points/{month}/{year}")
    public ResponseEntity<Object> processRewardPoints(@Valid RewardsReqDto rewardsReqDto){
        Map<String, Double> rewardPoints = rewardsService.getRewardsByMonthAndYear(rewardsReqDto.getMonth(), rewardsReqDto.getYear());

        var response = new ArrayList<>();
        rewardPoints.forEach((custId, totalRewards) -> response.add(new RewardResponseDetails(custId, totalRewards)));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cal-points")
    public ResponseEntity<Object> processRewardPoints(@Valid @RequestBody CustomerTransactionDto customerTransactionDto,
                                                      BindingResult result){
        if (result.hasErrors()) {
            return getErrorResponseEntity(result);
        }

        double rewardPoints = rewardsService.processRewards(customerTransactionDto);

        var response = new LinkedHashMap<>();
        response.put("CustomerId", customerTransactionDto.getCustomerId());
        response.put("InvoiceId", customerTransactionDto.getInvoiceId());
        response.put("TotalInvoiceAmount", customerTransactionDto.getTotalInvoiceAmount());
        response.put("RewardPoints", rewardPoints);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cal-points/v2")
    public ResponseEntity<Object> processRewardPoints(@Valid @RequestBody List<CustomerTransactionDto> customerTransactions,
                                                      BindingResult result){
        if (result.hasErrors()) {
            return getErrorResponseEntity(result);
        }

        Map<String, Double> rewardPoints = rewardsService.processRewards(customerTransactions);

        var response = new ArrayList<>();
        rewardPoints.forEach((custId, totalRewards) -> {
            response.add(new RewardResponseDetails(custId, totalRewards));
        });
        return ResponseEntity.ok(response);
    }
}


record RewardResponseDetails(String customerId, Double totalRewardPoints) {}
