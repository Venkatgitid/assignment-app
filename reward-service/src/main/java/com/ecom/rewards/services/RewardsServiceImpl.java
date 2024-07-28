package com.ecom.rewards.services;

import com.ecom.rewards.exceptions.CustomerRewardsNotFoundException;
import com.ecom.rewards.models.CustomerRewards;
import com.ecom.rewards.dto.CustomerTransactionDto;
import com.ecom.rewards.repositories.CustomerRewardsRepository;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsServiceImpl implements RewardsService{

    private final CustomerRewardsRepository rewardsRepository;

    public RewardsServiceImpl(CustomerRewardsRepository rewardsRepository) {
        this.rewardsRepository = rewardsRepository;
    }

    public double processRewards(CustomerTransactionDto customerTransactionDto) {
        double rewardPoints = calculateRewardPoints(customerTransactionDto.getTotalInvoiceAmount());
        CustomerRewards customerRewards = createCustomerRewardsFrom(customerTransactionDto);
        customerRewards.setRewardsPoint(rewardPoints);
        rewardsRepository.save(customerRewards);
        return rewardPoints;
    }

    private CustomerRewards createCustomerRewardsFrom(CustomerTransactionDto customerTransactionDto) {
        return CustomerRewards.builder()
                .customerId(customerTransactionDto.getCustomerId())
                .invoiceId(customerTransactionDto.getInvoiceId())
                .invoiceAmount(customerTransactionDto.getTotalInvoiceAmount())
                .invoiceDate(customerTransactionDto.getInvoiceDate())
                .build();
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

    @Override
    public Map<String, Double> getRewardsByMonthAndYear(int month, int year) {
        List<CustomerRewards> customerRewards = rewardsRepository.findRewardPointsByMonthAndYear(month, year);
        if (!customerRewards.isEmpty()) {
            return customerRewards.stream()
                    .collect(Collectors.groupingBy(CustomerRewards::getCustomerId, Collectors.summingDouble(CustomerRewards::getRewardsPoint)));
        } else {
            throw new CustomerRewardsNotFoundException(MessageFormat.format("No Rewards Found for the given Month : {0} and Year : {1}", String.valueOf(month), String.valueOf(year)));
        }
    }

    @Override
    public Map<String, Double> getRewardsByCustomerId(String customerId) {
        List<CustomerRewards> customerRewards = rewardsRepository.findRewardsPointsByCustomerId(customerId);
        if (!customerRewards.isEmpty()) {
            return customerRewards.stream()
                    .collect(Collectors.groupingBy(CustomerRewards::getCustomerId, Collectors.summingDouble(CustomerRewards::getRewardsPoint)));
        } else {
            throw new CustomerRewardsNotFoundException(MessageFormat.format("No Rewards Points Found for the given customer id : {0}", customerId));
        }
    }

    @Override
    public Map<String, Double> getRewardsByCustomerIdWithMonthAndYear(String customerId, int month, int year) {
        List<CustomerRewards> customerRewards = rewardsRepository.findRewardsPointsByCustomerIdAndMonthAndYear(customerId, month, year);
        if (!customerRewards.isEmpty()) {
            return customerRewards.stream()
                    .collect(Collectors.groupingBy(CustomerRewards::getCustomerId, Collectors.summingDouble(CustomerRewards::getRewardsPoint)));
        } else {
            throw new CustomerRewardsNotFoundException(MessageFormat.format("No Rewards Points Found for the given customer id : {0}", customerId));
        }
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
