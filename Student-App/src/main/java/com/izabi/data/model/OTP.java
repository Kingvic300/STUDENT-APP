package com.izabi.data.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Setter
@Getter
@Document
public class OTP {

    @Id
    private String id;
    private String otp;
    private String email;

    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used = false;
}
