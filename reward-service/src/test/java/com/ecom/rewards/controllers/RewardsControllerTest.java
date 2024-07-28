package com.ecom.rewards.controllers;

import com.ecom.rewards.dto.CustomerTransactionDto;
import com.ecom.rewards.dto.RewardResponseDto;
import com.ecom.rewards.services.RewardsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ecom.rewards.constants.AppConstants.REWARD_POINTS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(controllers = RewardsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsServiceImpl rewardsService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerTransactionDto customerTransactionDto1;
    private final List<CustomerTransactionDto> customerTransactionDtoList = new ArrayList<>();

    private static final Gson gson = new Gson();

    @BeforeEach
    public void init(){
        customerTransactionDto1 = CustomerTransactionDto.builder()
                .customerId("c1")
                .invoiceId(101L)
                .invoiceDate(LocalDate.now())
                .totalInvoiceAmount(120.0)
                .build();
        CustomerTransactionDto customerTransactionDto2 = CustomerTransactionDto.builder()
                .customerId("c2")
                .invoiceId(102L)
                .invoiceDate(LocalDate.now())
                .totalInvoiceAmount(99.0)
                .build();

        customerTransactionDtoList.add(customerTransactionDto1);
        customerTransactionDtoList.add(customerTransactionDto2);
    }

    @DisplayName("Test 1 : Process Customer Txn to calculate Reward Points")
    @Order(1)
    @Test
    public void test_processRewardPoints() throws Exception{
        Mockito.when(rewardsService.processRewards(customerTransactionDto1)).thenReturn(90.0);
        ResultActions response = mockMvc.perform(
                post("/rewards/cal-points")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerTransactionDto1))
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(90.0, Double.valueOf(result.get(REWARD_POINTS)));
    }

    @DisplayName("Test 2 : Process Customer Txn without customerId")
    @Order(2)
    @Test
    public void test_processRewardPointsWithoutCustomerId() throws Exception{
        CustomerTransactionDto customerTransactionDto = CustomerTransactionDto.builder().build();

        ResultActions response = mockMvc.perform(
                post("/rewards/cal-points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerTransactionDto))
        );

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals("must not be empty", result.get("customerId"));
    }

    @DisplayName("Test 3 : Process Multiple Customer Txn to calculate Reward Points")
    @Order(3)
    @Test
    public void test_processRewardPointsWithMultipleCustomerTxns() throws Exception{
        Map<String, Double> rewardPoints = Map.of("c1", 90.0, "c2", 49.0);
        Mockito.when(rewardsService.processRewards(customerTransactionDtoList)).thenReturn(rewardPoints);
        ResultActions response = mockMvc.perform(
                post("/rewards/cal-points/v2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerTransactionDtoList))
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<List<Map<String, String>>>() {}.getType();
        List<Map<String, String>> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(2, result.size());
    }

    @DisplayName("Test 4 : Get Rewards points By customer Id")
    @Order(4)
    @Test
    public void test_getRewardPointsByCustomerId() throws Exception{
        String testCustomerId = "c1";
        Mockito.when(rewardsService.getRewardsByCustomerId(testCustomerId)).thenReturn(Map.of(testCustomerId, 120.0));
        ResultActions response = mockMvc.perform(
                get("/rewards/pointsByCustomerId?customerId=c1")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<List<RewardResponseDto>>() {}.getType();
        List<RewardResponseDto> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(120, result.getFirst().totalRewardPoints());
    }

    @DisplayName("Test 5 : Get Rewards points By customer Id, Month and Year")
    @Order(5)
    @Test
    public void test_getRewardPointsByAllParam() throws Exception{
        String testCustomerId = "c1";
        int month = 7;
        int year = 2024;
        Mockito.when(rewardsService.getRewardsByCustomerIdWithMonthAndYear(testCustomerId, month, year)).thenReturn(Map.of(testCustomerId, 250.0));
        ResultActions response = mockMvc.perform(
                get("/rewards/pointsBy?customerId=c1&month=7&year=2024")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<List<RewardResponseDto>>() {}.getType();
        List<RewardResponseDto> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(250, result.getFirst().totalRewardPoints());
    }

    @DisplayName("Test 6 : Get Rewards points By Unknown Customer Id")
    @Order(6)
    @Test
    public void test_getRewardPointsByAllParamWithUnknownCustomerId() throws Exception{
        String testCustomerId = "c1";
        int month = 7;
        int year = 2024;
        Mockito.when(rewardsService.getRewardsByCustomerIdWithMonthAndYear(testCustomerId, month, year)).thenReturn(Map.of(testCustomerId, 250.0));
        ResultActions response = mockMvc.perform(
                get("/rewards/pointsBy?customerId=c12345&month=7&year=2024")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<List<RewardResponseDto>>() {}.getType();
        List<RewardResponseDto> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(0, result.size());
    }

    @DisplayName("Test 7 : Get Rewards points By Invalid Month")
    @Order(7)
    @Test
    public void test_getRewardPointsByAllParamWithInvalidMonth() throws Exception{
        ResultActions response = mockMvc.perform(
                get("/rewards/pointsBy?customerId=c12&month=15&year=2024")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        response.andExpect(MockMvcResultMatchers.status().isBadRequest());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Assertions.assertEquals("Invalid month : 15 or year : 2,024, month must be between 1 to 12 and year must be >= 2024", contentAsString);
    }

    @DisplayName("Test 8 : Get Rewards points By Month and Year")
    @Order(8)
    @Test
    public void test_getRewardPointsByMonthAndYear() throws Exception{
        String testCustomerId1 = "c1";
        String testCustomerId2 = "c2";
        int month = 7;
        int year = 2024;
        Mockito.when(rewardsService.getRewardsByMonthAndYear(month, year))
                .thenReturn(
                        Map.of(testCustomerId1, 250.0, testCustomerId2, 350.0)
                );
        ResultActions response = mockMvc.perform(
                get("/rewards/pointsByMonthAndYear/7/2024")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        response.andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        MvcResult mvcResult = response.andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        Type responseType = new TypeToken<List<RewardResponseDto>>() {}.getType();
        List<RewardResponseDto> result = gson.fromJson(contentAsString, responseType);
        Assertions.assertEquals(2, result.size());
        result.stream().filter(item -> item.customerId().equalsIgnoreCase("c2")).forEach(data -> {
            Assertions.assertEquals(350.0, data.totalRewardPoints());
        });
    }
}