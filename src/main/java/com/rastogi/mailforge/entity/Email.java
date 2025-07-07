package com.rastogi.mailforge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Email {

    @Id
    private String id;
    private String from;
    private String to;
    private String subject;
    private String body;
    private String sentAt;
    private String status;

}
