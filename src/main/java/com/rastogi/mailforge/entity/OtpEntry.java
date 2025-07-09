package com.rastogi.mailforge.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
public class OtpEntry {

    @Id
    private String id;
    private String userId;
    private String otp;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String phone;
}
