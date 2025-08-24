package com.izabi.service;

import com.izabi.data.enums.Role;
import com.izabi.data.model.Embedding;
import com.izabi.data.model.PendingUser;
import com.izabi.data.model.User;
import com.izabi.data.repository.EmbeddingRepository;
import com.izabi.data.repository.PendingUserRepository;
import com.izabi.data.repository.UserRepository;
import com.izabi.dto.request.*;
import com.izabi.dto.response.*;
import com.izabi.exception.*;
import com.izabi.mapper.UserMapper;
import com.izabi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final OTPService otpService;
    private final UserRepository userRepository;
    private final PendingUserRepository pendingUserRepository;
    private final JwtUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CloudinaryService cloudinaryService;
    private final VoiceAuthenticationService voiceAuthenticationService;
    private final EmbeddingRepository embeddingRepository;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public OTPResponse sendVerificationOTP(CreateUserRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            throw new AlreadyExistsException("Email Already in use");
        }
        validateRegisterRequest(request);
        validateEmail(request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        PendingUser pendingUser = new PendingUser();
        pendingUser.setEmail(request.getEmail());
        pendingUser.setPassword(encodedPassword);
        pendingUser.setOtp(otpService.sendOtp(request.getEmail()).getOtp());
        pendingUser.setRole(request.getRole());

        pendingUserRepository.save(pendingUser);

        return UserMapper.mapToOtpSentResponse( pendingUser.getOtp(),"OTP sent successfully. Please verify to complete registration.", request.getEmail());
    }

    @Override
    public CreatedUserResponse register(RegisterUserRequest request) {
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!pendingUser.getOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid OTP");
        }

        User user = new User();
        user.setEmail(pendingUser.getEmail());
        user.setPassword(pendingUser.getPassword());
        user.setRole(pendingUser.getRole());
        user.setRegistrationDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        userRepository.save(user);
        var jwtToken = jwtTokenUtil.generateToken(user);


        pendingUserRepository.delete(pendingUser);

        return UserMapper.mapToCreatedUserResponse(jwtToken,user,"Registration Successful");
    }

    @Override
    public UploadResponse uploadFile(MultipartFile file){
        String cloud;
        try{
        cloud = cloudinaryService.uploadFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return UserMapper.mapToUploadResponse("Image has been uploaded successfully", cloud);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), loginRequest.getPassword()));

        Optional<User> existingUser = userRepository.findByEmail(loginRequest.getEmail());
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found with email");
        }

        User user = existingUser.get();
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        if (!user.isActive()) {
            throw new IsNotActiveException("User is not active");
        }
        if(loginRequest.getRole() != null && !loginRequest.getRole().equals(user.getRole())) {
            throw new InvalidRoleException("Invalid role for user");
        }

        var jwtToken = jwtTokenUtil.generateToken(user);
        return UserMapper.mapToLoginResponse(jwtToken, "Login was successful", user);
    }

    @Override
    public UpdateUserProfileResponse updateProfile(UpdateUserProfileRequest updateUserProfileRequest) {
        Optional<User> existingUser = userRepository.findById(updateUserProfileRequest.getId());
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found with id");
        }
        User user = existingUser.get();
        if (!user.isActive()) {
            throw new IsNotActiveException("Account has been deactivated");
        }
        UserMapper.mapToUpdateProfile(updateUserProfileRequest, user);
        user.setPassword(passwordEncoder.encode(updateUserProfileRequest.getPassword()));
        userRepository.save(user);
        var token = jwtTokenUtil.generateToken(user);
        return UserMapper.mapToUpdateUserProfileResponse(token, "User profile updated successfully");
    }

    @Override
    public ResetPasswordResponse resetPassword(ChangePasswordRequest changePasswordRequest) {
        otpService.verifyOtp(changePasswordRequest.getEmail(), changePasswordRequest.getOtp());
        User user = userRepository.findByEmail(changePasswordRequest.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        otpService.deleteOtp(changePasswordRequest.getEmail(), changePasswordRequest.getOtp());
        return UserMapper.mapToResetPasswordResponse("Password reset successful",changePasswordRequest.getEmail());
    }

    @Override
    public ResetPasswordResponse sendResetOtp(ResetPasswordRequest resetPasswordRequest){
        Optional<User> user = userRepository.findByEmail(resetPasswordRequest.getEmail());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        otpService.sendOtp(resetPasswordRequest.getEmail());
        return UserMapper.mapToResetPasswordResponse("OTP sent Successfully", resetPasswordRequest.getEmail());
    }

    @Override
    public FoundResponse findUserById(String id){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()){
            throw new UserNotFoundException("User not found with id");
        }
        return UserMapper.mapToFoundResponse("User found", user.get().getId());
    }

    @Override
    public VoiceRegistrationResponse voiceSignup(VoiceSignupRequest request) {
        if (
                request.getEmail() == null || request.getEmail().isEmpty()
                || request.getVoiceSample() == null || request.getVoiceSample().isEmpty()
                || request.getRole() == null || request.getRole().isEmpty()){
            throw new IllegalArgumentException("Fill All field");
        }
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isPresent()){
            throw new AlreadyExistsException("Email in use");
        }
        try{
            EmbeddingResponse voicePrint = voiceAuthenticationService.extractVoiceFeatures(request.getVoiceSample());

            if(voicePrint == null){
                throw new VoiceProcessingFailedException("voice cannot be null");
            }
            Embedding embedding = new Embedding();
            embedding.setId(voicePrint.getEmbedding().getId());
            embedding.setCreatedAt(voicePrint.getEmbedding().getCreatedAt());
            embedding.setVoicePrint(voicePrint.getEmbedding().getVoicePrint());
            embeddingRepository.save(embedding);
            String generatedPassword = passwordEncoder.encode(voiceAuthenticationService.generateSecurePassword());

            PendingUser pendingUser =  new PendingUser();
            pendingUser.setEmail(request.getEmail());
            pendingUser.setVoicePrint(embedding.getVoicePrint());
            pendingUser.setPassword(generatedPassword);
            pendingUser.setOtp(otpService.sendOtp(request.getEmail()).getOtp());
            pendingUser.setRole(Role.valueOf(request.getRole()));
            pendingUserRepository.save(pendingUser);

            return UserMapper.mapToVoiceRegistrationResponse(
                    "Voice signup initiated. Please verify OTP to complete registration.",
                    request.getEmail(),
                    "Voice authentication enabled"
            );

        }catch(Exception e){
            throw new VoiceProcessingFailedException("failed to process voice");
        }

    }

    @Override
    public CreatedUserResponse completeVoiceRegistration(CompleteVoiceRegistrationRequest request) {
        PendingUser pendingUser = pendingUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("pending user not found"));
        if(!pendingUser.getOtp().equals(request.getOtp())){
            throw new InvalidOtpException("invalid otp");
        }
        User user = new User();
        user.setEmail(pendingUser.getEmail());
        user.setPassword(pendingUser.getPassword());
        user.setVoicePrint(pendingUser.getVoicePrint());
        user.setRole(pendingUser.getRole());
        user.setRegistrationDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(true);
        user.setVoiceAuthEnabled(true);
        userRepository.save(user);
        var jwtToken = jwtTokenUtil.generateToken(user);
        pendingUserRepository.delete(pendingUser);

        return UserMapper.mapToCreatedUserResponse(jwtToken, user, "Voice registration completed successfully");
    }

    @Override
    public LoginResponse voiceLogin(VoiceLoginRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("user not found");
        }

        User user = existingUser.get();

        if (!user.isActive()) {
            throw new InactiveUserException("User account is inactive");
        }

        if (!user.isVoiceAuthEnabled()) {
            throw new VoiceAuthenticationException("voice authentication not enabled");
        }

        try {
            boolean voiceMatched = voiceAuthenticationService.verifyVoice(request.getVoiceSample(), user.getVoicePrint());

            if (!voiceMatched) {
                throw new VoiceDoesNotMatchException("voice authentication failed");
            }

            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            var jwtToken = jwtTokenUtil.generateToken(user);
            return UserMapper.mapToLoginResponse(jwtToken, "Voice login successful", user);

        } catch (IOException e) {
            throw new VoiceProcessingFailedException("failed to process voice");
        }
    }


    @Override
    public LogoutResponse logout() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User user = existingUser.get();

        String token = jwtTokenUtil.extractTokenFromContext();

        if (token != null) {
            tokenBlacklistService.blacklistToken(token);
        }

        user.setLastLogoutDate(LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.clearContext();

        return UserMapper.mapToLogoutResponse("Logout successful", email);
    }

    @Override
    public LogoutResponse logoutFromAllDevices() {
        Authentication authentication = getAuthentication();

        String email = authentication.getName();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = existingUser.get();

        tokenBlacklistService.blacklistAllUserTokens(user.getId());

        user.setLastLogoutDate(LocalDateTime.now());
        userRepository.save(user);

        SecurityContextHolder.clearContext();

        return UserMapper.mapToLogoutResponse("Logged out from all devices successfully", email);    }

    @Override
    public VoiceAuthResponse enableVoiceAuthentication(EnableVoiceAuthRequest request) {
        Authentication authentication = getAuthentication();


        String email = authentication.getName();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = existingUser.get();
        if (user.isVoiceAuthEnabled()) {
            throw new VoiceAlreadyEnabledException("voice already enabled");
        }

        try {
            EmbeddingResponse voicePrint = voiceAuthenticationService.extractVoiceFeatures(request.getVoiceSample());
            if(voicePrint.getEmbedding() == null){
                throw new VoiceProcessingFailedException("voice cannot be null");
            }
            embeddingObjectCreation(user, voicePrint);

            return UserMapper.mapToVoiceAuthResponse("Voice authentication enabled successfully", email);

        } catch (IOException e) {
            log.error("Error processing voice sample for user: {}", email, e);
            throw new VoiceProcessingFailedException("Failed to process voice sample");
        }
    }

    @Override
    public VoiceAuthResponse disableVoiceAuthentication() {
        Authentication authentication = getAuthentication();
        String email = authentication.getName();
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = existingUser.get();

        if (!user.isVoiceAuthEnabled()) {
            throw new IllegalStateException("Voice authentication is already disabled");
        }

        user.setVoicePrint(null);
        user.setVoiceAuthEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return UserMapper.mapToVoiceAuthResponse("Voice authentication disabled successfully", email);
    }

    @Override
    public VoiceAuthResponse enrollVoiceSample(VoiceEnrollRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = existingUser.get();

        try {
            EmbeddingResponse voicePrint = voiceAuthenticationService.extractVoiceFeatures(request.getVoiceSample());

            if (voicePrint.getEmbedding() == null) {
                throw new VoiceProcessingFailedException("Failed to extract voice features");
            }

            embeddingObjectCreation(user, voicePrint);

            return UserMapper.mapToVoiceAuthResponse("Voice enrolled successfully", request.getEmail());

        } catch (IOException e) {
            throw new VoiceProcessingFailedException("Failed to process voice sample");
        }
    }

    private void embeddingObjectCreation(User user, EmbeddingResponse voicePrint) {
        Embedding embedding = new Embedding();
        embedding.setId(voicePrint.getEmbedding().getId());
        embedding.setCreatedAt(voicePrint.getEmbedding().getCreatedAt());
        embedding.setVoicePrint(voicePrint.getEmbedding().getVoicePrint());

        embeddingRepository.save(embedding);
        user.setVoicePrint(embedding.getVoicePrint());
        user.setVoiceAuthEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public VoiceAuthResponse verifyVoiceSample(VoiceVerifyRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        User user = existingUser.get();
        if (!user.isVoiceAuthEnabled() || user.getVoicePrint() == null) {
            throw new VoiceAuthenticationException("Voice not enrolled for user");
        }

        try {
            boolean matched = voiceAuthenticationService.verifyVoice(request.getVoiceSample(), user.getVoicePrint());
            if (!matched) {
                return UserMapper.mapToVoiceAuthResponse("Voice does not match", request.getEmail());
            }

            return UserMapper.mapToVoiceAuthResponse("Voice verified successfully", request.getEmail());

        } catch (IOException e) {
            throw new VoiceProcessingFailedException("Failed to process voice sample");
        }
    }



    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NoAuthenticatedUserException("No authenticated user found");
        }
        return authentication;
    }

    private void validateEmail(String email) {
        Optional<User> foundUser = userRepository.findByEmail(email);
        if (foundUser.isPresent()) {
            throw new AlreadyExistsException("PendingUser already registered");
        }
    }

    private void validateRegisterRequest(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("PendingUser cannot be empty");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }
}
