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

    @DisplayName("Test 1 : Save Customer Rewards")
    @Order(1)
    @Rollback(value = false)
    @Test
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

    @DisplayName("Test 2 : Fetch Customer Rewards")
    @Order(2)
    @Test
    public void getCustomerRewards(){
        Optional<CustomerRewards> customerRewards = customerRewardsRepository.findById(1L);
        Assertions.assertTrue(customerRewards.isPresent());

        List<CustomerRewards> customerRewardsList = customerRewardsRepository.findAll();
        Assertions.assertEquals(3, customerRewardsList.size());
    }

    @DisplayName("Test 3 : Find Rewards Points By CustomerId")
    @Order(3)
    @Test
    public void findRewardsPointsByCustomerId(){
        List<CustomerRewards> customerRewards = customerRewardsRepository.findRewardsPointsByCustomerId("c1");
        Assertions.assertEquals(2, customerRewards.size());
    }

    @DisplayName("Test 4 : Find Rewards Points By Month And Year")
    @Order(4)
    @Test
    public void findRewardPointsByMonthAndYear(){
        List<CustomerRewards> customerRewards = customerRewardsRepository.findRewardPointsByMonthAndYear(currentDate.getMonth().getValue(), currentDate.getYear());
        Assertions.assertEquals(2, customerRewards.size());
    }

    @DisplayName("Test 5 : Find Rewards Points By CustomerId With Month and Year")
    @Order(5)
    @Test
    public void findRewardsPointsByCustomerIdAndMonthAndYear(){
        List<CustomerRewards> customerRewards = customerRewardsRepository.findRewardsPointsByCustomerIdAndMonthAndYear("c2", previousMonthDate.getMonth().getValue(), previousMonthDate.getYear());
        Assertions.assertEquals(1, customerRewards.size());
    }

    @DisplayName("Test 6 : Find Rewards Points By Unknown CustomerId")
    @Order(6)
    @Test
    public void findRewardsPointsByUnknownCustomerId(){
        List<CustomerRewards> customerRewards = customerRewardsRepository.findRewardsPointsByCustomerId("c12345");
        Assertions.assertEquals(0, customerRewards.size());
    }
}