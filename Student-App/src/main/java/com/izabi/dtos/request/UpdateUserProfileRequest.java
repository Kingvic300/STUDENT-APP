package com.izabi.dtos.request;

import lombok.Data;

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
