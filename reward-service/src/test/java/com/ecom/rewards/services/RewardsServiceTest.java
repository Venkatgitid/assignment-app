package com.ecom.rewards.services;

import com.ecom.rewards.dto.CustomerTransactionDto;
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

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @DisplayName("Test the process reward calculation method with diff totalInvoiceAmount")
    @ParameterizedTest
    @ValueSource(doubles = {-1D, 0D, 1D, 49D, 50D, 51D, 99D, 100D, 101D, 120D}) // diff totalInvoiceAmount
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

    @DisplayName("Test the process reward calculation method with multiple invoice transaction")
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

    @DisplayName("Test to fetch the reward details by month and year")
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
}