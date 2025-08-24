package com.izabi.dto.request;

import com.izabi.data.enums.Role;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
public class UpdateUserProfileRequest {

    private String id;
    private String firstName;
    private String email;
    private String phoneNumber;
    private String lastName;
    private String institution;
    private String major;
    private String location;
    private String profilePicturePath;
}
