package com.izabi.dto.request;

import com.izabi.data.enums.Role;
import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private Role role;
}
