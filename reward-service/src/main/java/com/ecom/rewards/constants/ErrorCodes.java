package com.ecom.rewards.constants;

import lombok.Getter;

@Getter
public enum ErrorCodes {
    ERROR_10000(10000, "Resource Not Found");

    private final Integer errorCode;
    private final String errorDesc;

    ErrorCodes(Integer errorCode, String errorDesc) {
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }
}
