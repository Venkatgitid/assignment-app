package com.ecom.rewards.controllers;

import com.ecom.rewards.dto.CustomerTransactionDto;
import com.ecom.rewards.dto.RewardResponseDto;
import com.ecom.rewards.dto.RewardsReqDto;
import com.ecom.rewards.services.RewardsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.text.MessageFormat;
import java.util.*;

import static com.ecom.rewards.utils.ControllerUtils.getErrorResponseEntity;


@Slf4j
@RestController
@RequestMapping("/rewards")
public class RewardsController {

    private final RewardsService rewardsService;

    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
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
        rewardPoints.forEach((custId, totalRewards) -> response.add(new RewardResponseDto(custId, totalRewards)));
        return ResponseEntity.ok(response);
    }

   @GetMapping("/pointsByCustomerId")
    public ResponseEntity<Object> getRewardPointsByCustomerId(@RequestParam(name = "customerId") String customerId){
        Map<String, Double> rewardPoints = rewardsService.getRewardsByCustomerId(customerId);

        var response = new ArrayList<>();
        rewardPoints.forEach((custId, totalRewards) -> response.add(new RewardResponseDto(custId, totalRewards)));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pointsByMonthAndYear/{month}/{year}")
    public ResponseEntity<Object> getRewardPointsByMonthAndYear(@Valid RewardsReqDto rewardsReqDto){
        Map<String, Double> rewardPoints = rewardsService.getRewardsByMonthAndYear(rewardsReqDto.getMonth(), rewardsReqDto.getYear());

        var response = new LinkedList<>();
        rewardPoints.forEach((custId, totalRewards) -> response.add(new RewardResponseDto(custId, totalRewards)));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/pointsBy")
    public ResponseEntity<Object> getRewardPointsByAllParam(@RequestParam(name = "customerId") String customerId,
                                                      @RequestParam(name = "month", defaultValue = "0", required = false) int month,
                                                      @RequestParam(name = "year", defaultValue = "0", required = false) int year){
        if (month <= 0 || month > 12 || year <= 0) {
            return ResponseEntity.badRequest()
                    .body(MessageFormat.format("Invalid month : {0} or year : {1}, month must be between 1 to 12 and year must be >= 2024", month, year));
        }

        Map<String, Double> rewardPoints = rewardsService.getRewardsByCustomerIdWithMonthAndYear(customerId, month, year);

        var response = new ArrayList<>();
        rewardPoints.forEach((custId, totalRewards) -> response.add(new RewardResponseDto(custId, totalRewards)));

        return ResponseEntity.ok(response);
    }
}


