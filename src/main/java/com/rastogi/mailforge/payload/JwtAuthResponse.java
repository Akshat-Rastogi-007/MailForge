package com.rastogi.mailforge.payload;

import com.rastogi.mailforge.dto.respose.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtAuthResponse {

    private String token;
    private UserResponseDto userResponseDto;

}
