package com.rastogi.mailforge.dto.request;


import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PhoneVerificationDto {


    @Pattern(regexp = "^[+]?91[6-9]\\d{9}$", message = "Invalid phone number")
    private String phone;

}
