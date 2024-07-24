package com.ecom.rewards.exceptions;

import com.ecom.rewards.constants.ErrorCodes;
import com.ecom.rewards.dto.ApiErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(value = {CustomerRewardsNotFoundException.class})
    public ResponseEntity<ApiErrorResponseDto> handleResourceNotFoundException(CustomerRewardsNotFoundException ex, WebRequest request){
        return new ResponseEntity<>(ApiErrorResponseDto.builder()
                .errorCode(ErrorCodes.ERROR_10000.getErrorCode())
                .errorDesc(ErrorCodes.ERROR_10000.getErrorDesc())
                .errorMessage(ex.getMessage())
                .errorDateTime(LocalDateTime.now())
                .build(), HttpStatus.BAD_REQUEST);
    }
}
