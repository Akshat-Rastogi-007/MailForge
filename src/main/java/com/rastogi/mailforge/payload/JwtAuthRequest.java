package com.rastogi.mailforge.payload;
import lombok.Data;

@Data
public class JwtAuthRequest {

    private String emailAddress;
    private String password;

}
