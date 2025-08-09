package com.izabi.data.model;

import com.izabi.data.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Setter
@Getter
@Document
public class PendingUser {

    @Id
    private String id;
    private String email;
    private String password;
    private String voicePrint;
    private String otp;
    private LocalDateTime expiryTime;
    private Role role;
}
