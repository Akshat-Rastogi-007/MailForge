package com.rastogi.mailforge.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Email {

    @Id
    private String id;
    private String senderAddress;
    private String receiverAddress;
    private String subject;
    private String body;
    private String sentAt;
    private String status;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;
}
