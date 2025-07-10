package com.rastogi.mailforge.dto.respose;


import lombok.Data;

@Data
public class MailResponseDto {
    private String senderAddress;
    private String receiverAddress;
    private String subject;
    private String body;
    private String sentAt;
}
