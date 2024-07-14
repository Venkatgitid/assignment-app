package com.ecom.rewards.services;

import com.ecom.rewards.models.CustomerTransactionDto;

import java.util.List;
import java.util.Map;

public interface RewardsService {
    double processRewards(CustomerTransactionDto customerTransactionDto);
    Map<String, Double> processRewards(List<CustomerTransactionDto> customerTransactions);
}
