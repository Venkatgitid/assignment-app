package com.ecom.rewards.repositories;

import com.ecom.rewards.models.CustomerRewards;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerRewardsRepositoryTest {

    @Autowired
    private CustomerRewardsRepository customerRewardsRepository;

    LocalDate currentDate = LocalDate.now();
    LocalDate previousMonthDate = LocalDate.now().minusMonths(1);

    @Test
    @DisplayName("Test 1:Save Customer Rewards Test")
    @Order(1)
    @Rollback(value = false)
    public void saveCustomerRewards(){
        CustomerRewards customerRewards1 = CustomerRewards.builder()
                .customerId("c1")
                .invoiceId(123)
                .invoiceAmount(123.5)
                .invoiceDate(currentDate)
                .build();
        customerRewardsRepository.save(customerRewards1);
        System.out.println("customerRewards = " + customerRewards1);
        Assertions.assertTrue(customerRewards1.getId() > 0);

        CustomerRewards customerRewards2 = CustomerRewards.builder()
                .customerId("c1")
                .invoiceId(124)
                .invoiceAmount(234.50)
                .invoiceDate(currentDate)
                .build();
        customerRewardsRepository.save(customerRewards2);

        CustomerRewards customerRewards3 = CustomerRewards.builder()
                .customerId("c2")
                .invoiceId(222)
                .invoiceAmount(546)
                .invoiceDate(previousMonthDate)
                .build();
        customerRewardsRepository.save(customerRewards3);
    }

    @Test
    @DisplayName("Test 2:Fetch Customer Rewards Test")
    @Order(2)
    public void getCustomerRewards(){
        Optional<CustomerRewards> customerRewards = customerRewardsRepository.findById(1L);
        Assertions.assertTrue(customerRewards.isPresent());

        List<CustomerRewards> customerRewardsList = customerRewardsRepository.findAll();
        Assertions.assertEquals(3, customerRewardsList.size());
    }

    @Test
    @DisplayName("Test 3:Find RewardPoints By Month And Year")
    @Order(2)
    public void findRewardPointsByMonthAndYear(){
        List<CustomerRewards> customerRewards = customerRewardsRepository.findRewardPointsByMonthAndYear(currentDate.getMonth().getValue(), currentDate.getYear());
        Assertions.assertEquals(2, customerRewards.size());
    }
}