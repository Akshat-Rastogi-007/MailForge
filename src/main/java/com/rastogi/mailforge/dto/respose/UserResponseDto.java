package com.rastogi.mailforge.dto.respose;

import com.rastogi.mailforge.entity.Email;
import lombok.Data;

import java.util.List;

@Data
public class UserResponseDto {

    private String name;
    private String mailAddress;
    private String phone;
    private List<Email> sentMails;
    private List<Email> receivedMails;
    private Boolean verified;

}
