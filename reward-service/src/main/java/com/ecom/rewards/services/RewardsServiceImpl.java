package com.ecom.rewards.services;

import com.ecom.rewards.models.CustomerTransactionDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RewardsServiceImpl implements RewardsService{

    public double processRewards(CustomerTransactionDto customerTransactionDto) {
        return calculateRewardPoints(customerTransactionDto.getTotalInvoiceAmount());
    }

    @Override
    public Map<String, Double> processRewards(List<CustomerTransactionDto> customerTransactions) {
        Map<String, Double> totalRewards = new HashMap<>();
        customerTransactions.forEach(txn -> {
            double rewards = processRewards(txn);
            totalRewards.computeIfPresent(txn.getCustomerId(), (cid, existingRewards) -> existingRewards + rewards);
            totalRewards.computeIfAbsent(txn.getCustomerId(), (cid) -> rewards);
        });
        return totalRewards;
    }

    private double calculateRewardPoints(Double totalInvoiceAmount) {
        double rewardPoints = 0L;

        if (totalInvoiceAmount <= 50.0) {
            return rewardPoints;
        }

        if (totalInvoiceAmount <=100.0) {
            return totalInvoiceAmount - 50;
        }

        rewardPoints = 50.0;
        rewardPoints += Math.abs(totalInvoiceAmount - 100) * 2;
        return rewardPoints;
    }
}
