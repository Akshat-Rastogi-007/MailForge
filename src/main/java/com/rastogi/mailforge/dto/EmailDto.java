package com.rastogi.mailforge.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmailDto {

    private String receiverAddress;
    private String subject;
    private String body;
    private LocalDateTime scheduledTime;
}
