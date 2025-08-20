package com.izabi.mapper;

import com.izabi.data.model.User;
import com.izabi.dto.request.CreateUserRequest;
import com.izabi.dto.request.UpdateUserProfileRequest;
import com.izabi.dto.response.*;
import com.izabi.util.EmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class UserMapper {

    public static User mapToUser(CreateUserRequest createUserRequest) {
        User user = new User();
        user.setPassword(createUserRequest.getPassword());
        user.setEmail(EmailVerification.emailVerification(createUserRequest.getEmail()));
        user.setRole(createUserRequest.getRole());
        return user;
    }
    public static void mapToUpdateProfile(UpdateUserProfileRequest updateUserProfileRequest, User user) {
        user.setFirstName(updateUserProfileRequest.getFirstName());
        user.setLastName(updateUserProfileRequest.getLastName());
        user.setPhoneNumber(updateUserProfileRequest.getPhoneNumber());
        user.setLocation(updateUserProfileRequest.getLocation());
        user.setProfilePicturePath(updateUserProfileRequest.getProfilePicturePath());
        user.setInstitution(updateUserProfileRequest.getInstitution());
        user.setMajor(updateUserProfileRequest.getMajor());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(user.isActive());
    }
    public static UpdateUserProfileResponse mapToUpdateUserProfileResponse(String token, String message) {
        UpdateUserProfileResponse updateUserProfileResponse = new UpdateUserProfileResponse();
        updateUserProfileResponse.setMessage(message);
        updateUserProfileResponse.setToken(token);
        return updateUserProfileResponse;
    }
    public static CreatedUserResponse mapToCreatedUserResponse(String jwtToken, User user, String message) {
        CreatedUserResponse createdUserResponse = new CreatedUserResponse();
        createdUserResponse.setUser(user);
        createdUserResponse.setMessage(message);
        createdUserResponse.setJwtToken(jwtToken);
        return createdUserResponse;
    }
    public static LoginResponse mapToLoginResponse(String jwtToken, String message, User user) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(jwtToken);
        loginResponse.setMessage(message);
        loginResponse.setUser(user);
        loginResponse.setUserId(user.getId());
        loginResponse.setRole(user.getRole());
        return loginResponse;
    }
    public static ResetPasswordResponse mapToResetPasswordResponse(String message, String email) {
        ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse();
        resetPasswordResponse.setMessage(message);
        resetPasswordResponse.setEmail(email);
        return resetPasswordResponse;
    }
    public static UploadResponse mapToUploadResponse(String message, String cloudinaryUrl){
        UploadResponse uploadResponse = new UploadResponse();
        uploadResponse.setMessage(message);
        uploadResponse.setCloudinaryUrl(cloudinaryUrl);
        return uploadResponse;
    }

    public static OTPResponse mapToOtpSentResponse(String otp, String email, String message) {
        return OTPMapper.mapToOTPResponse(otp, message, email);
    }
    public static FoundResponse mapToFoundResponse(String message, String id){
        FoundResponse foundResponse = new FoundResponse();
        foundResponse.setMessage(message);
        foundResponse.setId(id);
        return foundResponse;
    }

    public static VoiceRegistrationResponse mapToVoiceRegistrationResponse(String message, String email, String status) {
        VoiceRegistrationResponse voiceRegistrationResponse = new VoiceRegistrationResponse();
        voiceRegistrationResponse.setEmail(email);
        voiceRegistrationResponse.setMessage(message);
        voiceRegistrationResponse.setStatus(status);
        return voiceRegistrationResponse;
    }

    public static LogoutResponse mapToLogoutResponse(String message, String email) {
        LogoutResponse logoutResponse = new LogoutResponse();
        logoutResponse.setEmail(email);
        logoutResponse.setMessage(message);
        return logoutResponse;
    }

    public static VoiceAuthResponse mapToVoiceAuthResponse(String message, String email) {
        VoiceAuthResponse voiceAuthResponse = new VoiceAuthResponse();
        voiceAuthResponse.setEmail(email);
        voiceAuthResponse.setMessage(message);
        return voiceAuthResponse;
    }
}
