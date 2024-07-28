package com.ecom.rewards.services;

import com.ecom.rewards.dto.CustomerTransactionDto;
import com.ecom.rewards.exceptions.CustomerRewardsNotFoundException;
import com.ecom.rewards.models.CustomerRewards;
import com.ecom.rewards.repositories.CustomerRewardsRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SpringBootTest
class RewardsServiceTest {

    @Mock
    private CustomerRewardsRepository rewardsRepository;
    private RewardsServiceImpl rewardsService;
    private AutoCloseable autoCloseable;
    private CustomerTransactionDto customerTransactionDto;

    @BeforeAll
    static void init() {
    }

    @BeforeEach
    void setup() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        rewardsService = new RewardsServiceImpl(rewardsRepository);
        customerTransactionDto = CustomerTransactionDto.builder()
                .customerId("C123")
                .invoiceId(12345L)
                .build();
    }

    @DisplayName("Test 1 : Process reward calculation method with diff totalInvoiceAmount")
    @Order(1)
    @ValueSource(doubles = {-1D, 0D, 1D, 49D, 50D, 51D, 99D, 100D, 101D, 120D}) // diff totalInvoiceAmount
    @ParameterizedTest
    void processRewards(double totalInvoiceAmount) {
        customerTransactionDto.setTotalInvoiceAmount(totalInvoiceAmount);
        double rewardPoints = rewardsService.processRewards(customerTransactionDto);
        String inputParam = String.valueOf(totalInvoiceAmount);
        switch (inputParam) {
            case "-1.0", "0.0", "1.0", "49.0", "50.0" -> Assertions.assertEquals(0D, rewardPoints);
            case "51.0" -> Assertions.assertEquals(1D, rewardPoints);
            case "99.0" -> Assertions.assertEquals(49D, rewardPoints);
            case "100.0" -> Assertions.assertEquals(50D, rewardPoints);
            case "101.0" -> Assertions.assertEquals(52D, rewardPoints);
            case "120.0" -> Assertions.assertEquals(90D, rewardPoints);
            default -> throw new IllegalStateException("Unexpected value: " + inputParam);
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @DisplayName("Test 2 : Process reward calculation method with multiple invoice transaction")
    @Order(2)
    @Test
    void processRewards_With_Multiple_Invoice() {
        List<CustomerTransactionDto> customerTransactions = new ArrayList<>();
        customerTransactions.add(CustomerTransactionDto.builder().customerId("C123").invoiceId(12345L).totalInvoiceAmount(1D).build());
        customerTransactions.add(CustomerTransactionDto.builder().customerId("C123").invoiceId(12346L).totalInvoiceAmount(49D).build());
        customerTransactions.add(CustomerTransactionDto.builder().customerId("C123").invoiceId(12347L).totalInvoiceAmount(99D).build());
        customerTransactions.add(CustomerTransactionDto.builder().customerId("C123").invoiceId(12348L).totalInvoiceAmount(100D).build());

        customerTransactions.add(CustomerTransactionDto.builder().customerId("C777").invoiceId(12349L).totalInvoiceAmount(120D).build());
        customerTransactions.add(CustomerTransactionDto.builder().customerId("C777").invoiceId(12350L).totalInvoiceAmount(51D).build());

        Map<String, Double> rewardPoints = rewardsService.processRewards(customerTransactions);
        Assertions.assertEquals(99D, rewardPoints.get("C123"));
        Assertions.assertEquals(91D, rewardPoints.get("C777"));

    }

    @DisplayName("Test 3 : Fetch the reward details by customerId")
    @Order(3)
    @Test
    void getRewardsByCustomerId() {
        String testCustomerId = "c1";
        CustomerRewards customerRewards1 = CustomerRewards.builder().customerId(testCustomerId).rewardsPoint(100).build();
        CustomerRewards customerRewards2 = CustomerRewards.builder().customerId(testCustomerId).rewardsPoint(150).build();
        Mockito.when(rewardsRepository.findRewardsPointsByCustomerId("c1"))
                .thenReturn(List.of(
                        customerRewards1,
                        customerRewards2
                ));

        Map<String, Double> rewardsByCustomerId = rewardsService.getRewardsByCustomerId(testCustomerId);
        Assertions.assertEquals(250, rewardsByCustomerId.get(testCustomerId));
    }

    @DisplayName("Test 4 : Fetch the reward details by unknown customerId")
    @Order(4)
    @Test()
    void getRewardsByUnknownCustomerId() {
        String testCustomerId = "c12345";
        String errorMessage = "No Rewards Points Found for the given customer id : c12345";
        Mockito.when(rewardsRepository.findRewardsPointsByCustomerId(testCustomerId))
                .thenReturn(Collections.emptyList());

        CustomerRewardsNotFoundException customerRewardsNotFoundException = Assertions.assertThrows(CustomerRewardsNotFoundException.class, () -> {
            rewardsService.getRewardsByCustomerId(testCustomerId);
        }, errorMessage);

        Assertions.assertEquals(errorMessage, customerRewardsNotFoundException.getMessage());
    }

    @DisplayName("Test 5 : Fetch the reward details by month and year")
    @Order(5)
    @Test
    void getRewardsByMonthAndYear() {
        int month = 7;
        int year = 2024;

        CustomerRewards customerRewards1 = CustomerRewards.builder().customerId("c1").rewardsPoint(100).build();
        CustomerRewards customerRewards2 = CustomerRewards.builder().customerId("c2").rewardsPoint(150).build();
        Mockito.when(rewardsRepository.findRewardPointsByMonthAndYear(month, year))
                .thenReturn(List.of(
                        customerRewards1,
                        customerRewards2
                ));

        Map<String, Double> rewardsByMonthAndYear = rewardsService.getRewardsByMonthAndYear(7, 2024);
        Assertions.assertEquals(100, rewardsByMonthAndYear.get(customerRewards1.getCustomerId()));
        Assertions.assertEquals(150, rewardsByMonthAndYear.get(customerRewards2.getCustomerId()));
    }

    @DisplayName("Test 6 : Fetch the reward details by unknown month and year")
    @Order(6)
    @Test()
    void getRewardsByUnknownMonthAndYear() {
        int month = 10;
        int year = 2000;
        String errorMessage = "No Rewards Found for the given Month : 10 and Year : 2000";
        Mockito.when(rewardsRepository.findRewardPointsByMonthAndYear(month, year))
                .thenReturn(Collections.emptyList());

        CustomerRewardsNotFoundException customerRewardsNotFoundException = Assertions.assertThrows(CustomerRewardsNotFoundException.class, () -> {
            rewardsService.getRewardsByMonthAndYear(month, year);
        }, errorMessage);

        Assertions.assertEquals(errorMessage, customerRewardsNotFoundException.getMessage());
    }

    @DisplayName("Test 7 : Fetch the reward details by customerId with month and year")
    @Order(7)
    @Test
    void getRewardsByCustomerIdWithMonthAndYear() {
        String testCustomerId = "c1";
        int month = 7;
        int year = 2024;

        CustomerRewards customerRewards1 = CustomerRewards.builder().customerId(testCustomerId).rewardsPoint(100).build();
        CustomerRewards customerRewards2 = CustomerRewards.builder().customerId(testCustomerId).rewardsPoint(150).build();
        CustomerRewards customerRewards3 = CustomerRewards.builder().customerId(testCustomerId).rewardsPoint(250).build();
        Mockito.when(rewardsRepository.findRewardsPointsByCustomerIdAndMonthAndYear(testCustomerId, month, year))
                .thenReturn(List.of(
                        customerRewards1,
                        customerRewards2,
                        customerRewards3
                ));

        Map<String, Double> rewardsByCustomerId = rewardsService.getRewardsByCustomerIdWithMonthAndYear(testCustomerId, month, year);
        Assertions.assertEquals(500, rewardsByCustomerId.get(testCustomerId));
    }
}