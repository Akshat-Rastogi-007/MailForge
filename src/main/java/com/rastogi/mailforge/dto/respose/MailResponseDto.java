package com.rastogi.mailforge.dto.respose;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MailResponseDto {
    private String senderAddress;
    private String receiverAddress;
    private String subject;
    private String body;
    private LocalDateTime sentAt;

}
