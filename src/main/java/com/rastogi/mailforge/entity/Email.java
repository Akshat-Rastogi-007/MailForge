package com.rastogi.mailforge.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Email {

    @Id
    private String id;
    private String receiverAddress;
    private String subject;
    private String body;

    @Lob
    private String wrappedKeyForSender;

    @Lob
    private String wrappedKeyForReceiver;


    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private boolean isDeleted = false;
    private boolean isRead = false;
    private String sentAt;
    
    private boolean deleteForSender = false;
    private boolean deleteForReceiver = false;
}
