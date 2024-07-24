package com.ecom.rewards.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiErrorResponseDto {
    private Integer errorCode;
    private String errorDesc;
    private String errorMessage;
    private LocalDateTime errorDateTime;
}
