package com.izabi.dtos.request;

import com.izabi.data.enums.Role;
import lombok.Data;

@Data
public class RegisterUserRequest {
    private String email;
    private String otp;
    private Role role;
}
