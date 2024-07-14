package com.ecom.rewards.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.HashMap;

public class ControllerUtils {
    public static ResponseEntity<Object> getErrorResponseEntity(BindingResult result) {
        var errorsList = result.getAllErrors();
        var errorsMap = new HashMap<String, String>();

        for (var errorObj : errorsList) {
            FieldError error = (FieldError) errorObj;
            errorsMap.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errorsMap);
    }
}
