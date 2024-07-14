package com.ecom.rewards.models;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerTransactionDto {

    @NotEmpty
    private String customerId;

    @Positive
    private Long invoiceId;

    @PositiveOrZero
    private Double totalInvoiceAmount;
}
