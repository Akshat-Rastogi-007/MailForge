package com.rastogi.mailforge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String name;
    private String password;
    private String mailAddress;
    private String phone;

    @OneToMany(mappedBy = "sender")
    private List<Email> sentMails;

    @OneToMany(mappedBy = "receiver")
    private List<Email> receivedMails;
}
