package com.izabi.dto.response;

import com.izabi.data.model.User;
import lombok.Data;

@Data
public class CreatedUserResponse {
    private User user;
    private String message;
    private String otp;
    private String jwtToken;

}
