package com.ecom.rewards.services;

import com.ecom.rewards.dto.CustomerTransactionDto;

import java.util.List;
import java.util.Map;

public interface RewardsService {
    double processRewards(CustomerTransactionDto customerTransactionDto);
    Map<String, Double> processRewards(List<CustomerTransactionDto> customerTransactions);
    Map<String, Double> getRewardsByMonthAndYear(int month, int year);
    Map<String, Double> getRewardsByCustomerId(String customerId);
    Map<String, Double> getRewardsByCustomerIdWithMonthAndYear(String customerId, int month, int year);
}
