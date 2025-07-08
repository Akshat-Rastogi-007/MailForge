package com.rastogi.mailforge.dto;

import lombok.Data;

@Data
public class EmailDto {
    private String senderAddress;
    private String receiverAddress;
    private String subject;
    private String body;
}
