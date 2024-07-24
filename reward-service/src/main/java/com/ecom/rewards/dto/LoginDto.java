package com.ecom.rewards.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginDto {

    @NotEmpty
    private String userName;

    @NotEmpty
    private String password;

}
