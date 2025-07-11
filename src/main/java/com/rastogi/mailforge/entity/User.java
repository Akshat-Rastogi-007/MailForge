package com.rastogi.mailforge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    private String id;
    private String name;
    private String password;
    private String mailAddress;
    private String phone;

    private List<String> role = new ArrayList<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Email> sentMails = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER)
    @JsonIgnore
    private List<Email> receivedMails = new ArrayList<>();

    private boolean verified = false;

    @Lob
    private String publicKey;

    @Lob
    private String privateKey;
}
