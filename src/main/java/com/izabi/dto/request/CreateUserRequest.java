package com.izabi.dto.request;

import com.izabi.data.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {
    private String email;
    private String password;
    private Role role;
}
