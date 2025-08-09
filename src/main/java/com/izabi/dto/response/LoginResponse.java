package com.izabi.dto.response;

import com.izabi.data.enums.Role;
import com.izabi.data.model.User;
import lombok.Data;

@Data
public class LoginResponse {
    private User user;
    private Role role;
    private String userId;
    private String token;
    private String message;
}
