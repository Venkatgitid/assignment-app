package com.ecom.rewards.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RewardsReqDto {
    @Min(1) @Max(12)
    private int month;

    @Min(1000) @Max(9999)
    private int year;
}
