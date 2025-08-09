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
import com.izabi.util.JwtUtil;
import com.izabi.service.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private PendingUserRepository pendingUserRepository;

    @Mock
    private JwtUtil jwtTokenUtil;

    @Mock
    private OTPService otpService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private VoiceAuthenticationService voiceAuthenticationService;

    @Mock
    private EmbeddingRepository embeddingRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createUserRequest;
    private RegisterUserRequest registerUserRequest;
    private LoginRequest loginRequest;
    private UpdateUserProfileRequest updateUserProfileRequest;
    private ChangePasswordRequest changePasswordRequest;
    private ResetPasswordRequest resetPasswordRequest;
    private VoiceSignupRequest voiceSignupRequest;
    private CompleteVoiceRegistrationRequest completeVoiceRegistrationRequest;
    private VoiceLoginRequest voiceLoginRequest;
    private EnableVoiceAuthRequest enableVoiceAuthRequest;
    private User user;
    private PendingUser pendingUser;
    private Embedding embedding;

    @BeforeEach
    void setUp() {
        List<Double> voicePrint = Arrays.asList(
                -179.73248291015625, 80.432373046875, 25.7293643951416, 26.481021881103516,
                0.9633487462997437, 9.07381534576416, -0.821174144744873, -0.6693892478942871,
                -0.7973114848136902, 6.839402198791504, -1.3271806240081787, 2.139167070388794,
                -3.3540587425231934, 150.9971923828125, 28.2651309967041, 30.896163940429688,
                15.999171257019043, 15.818509101867676, 10.698274612426758, 10.30055046081543,
                9.566656112670898, 9.09653091430664, 9.800527572631836, 8.17348861694336,
                9.797332763671875, 7.144084930419922, 1764.7723388671875, 758.12841796875,
                3716.839111328125, 1590.5218505859375, 21.500165939331055, 17.72335433959961,
                20.96478843688965, 19.498722076416016, 18.552955627441406, 17.707551956176758,
                53.31694030761719, 0.11314080655574799, 0.07901652157306671, 0.22481265664100647,
                0.3495051860809326, 0.532171368598938, 0.33286482095718384, 0.35310232639312744,
                0.2797504663467407, 0.43715015053749084, 0.41605344414711, 0.3430567681789398,
                0.4373064935207367, 0.2796345353126526, 0.2925730347633362, 0.20705638825893402,
                0.0326019749045372, 0.01897321827709675, -0.09110479801893234, -0.002287629758939147,
                -0.005856381729245186, 125.0
        );
        createUserRequest = new CreateUserRequest();
        createUserRequest.setEmail("test@example.com");
        createUserRequest.setPassword("password123");
        createUserRequest.setRole(Role.USER);

        registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setEmail("test@example.com");
        registerUserRequest.setOtp("123456");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
        loginRequest.setRole(Role.USER);

        updateUserProfileRequest = new UpdateUserProfileRequest();
        updateUserProfileRequest.setFirstName("John");
        updateUserProfileRequest.setLastName("Doe");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setEmail("test@example.com");
        changePasswordRequest.setOtp("123456");
        changePasswordRequest.setNewPassword("newPassword123");

        resetPasswordRequest = new ResetPasswordRequest();
        resetPasswordRequest.setEmail("test@example.com");

        voiceSignupRequest = new VoiceSignupRequest();
        voiceSignupRequest.setEmail("test@example.com");
        voiceSignupRequest.setRole("USER");
        voiceSignupRequest.setVoiceSample(mock(MultipartFile.class));

        completeVoiceRegistrationRequest = new CompleteVoiceRegistrationRequest();
        completeVoiceRegistrationRequest.setEmail("test@example.com");
        completeVoiceRegistrationRequest.setOtp("123456");

        voiceLoginRequest = new VoiceLoginRequest();
        voiceLoginRequest.setEmail("test@example.com");
        voiceLoginRequest.setVoiceSample(mock(MultipartFile.class));

        enableVoiceAuthRequest = new EnableVoiceAuthRequest();
        enableVoiceAuthRequest.setVoiceSample(mock(MultipartFile.class));

        user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
        user.setActive(true);
        user.setVoiceAuthEnabled(false);
        user.setRegistrationDate(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        pendingUser = new PendingUser();
        pendingUser.setEmail("test@example.com");
        pendingUser.setPassword("encodedPassword");
        pendingUser.setOtp("123456");
        pendingUser.setRole(Role.USER);
        pendingUser.setVoicePrint(voicePrint.toString());

        embedding = new Embedding();
        embedding.setId("123");
        embedding.setCreatedAt(LocalDateTime.parse("2024-01-01T10:00:00"));
        embedding.setVoicePrint(voicePrint.toString());

    }

    @Test
    void sendVerificationOTP_Success() {
        // Arrange
        // Fix: Configure mock to be called twice - once for the main method and once for validateEmail
        when(userRepository.findByEmail(createUserRequest.getEmail()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserRequest.getPassword()))
                .thenReturn("encodedPassword");
        when(otpService.sendOtp(createUserRequest.getEmail()))
                .thenReturn(new OTPResponse("123456", "test@example.com", "OTP sent"));
        when(pendingUserRepository.save(any(PendingUser.class)))
                .thenReturn(new PendingUser());

        // Act
        OTPResponse response = userService.sendVerificationOTP(createUserRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("OTP sent successfully. Please verify to complete registration.", response.getMessage()),
                () -> assertEquals("test@example.com", response.getEmail())
        );

        // Fix: Verify that findByEmail is called twice
        verify(userRepository, times(2)).findByEmail(createUserRequest.getEmail());
        verify(passwordEncoder).encode(createUserRequest.getPassword());
        verify(otpService).sendOtp(createUserRequest.getEmail());
        verify(pendingUserRepository).save(any(PendingUser.class));
    }

    @Test
    void sendVerificationOTP_EmailAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(createUserRequest.getEmail()))
                .thenReturn(Optional.of(user));

        // Act & Assert
        AlreadyExistsException exception = assertThrows(
                AlreadyExistsException.class,
                () -> userService.sendVerificationOTP(createUserRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail(createUserRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(otpService, never()).sendOtp(anyString());
    }

    @Test
    void sendVerificationOTP_InvalidEmail() {
        // Arrange
        createUserRequest.setEmail("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.sendVerificationOTP(createUserRequest));
    }

    @Test
    void sendVerificationOTP_NullEmail() {
        // Arrange
        createUserRequest.setEmail(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.sendVerificationOTP(createUserRequest));
    }

    @Test
    void sendVerificationOTP_InvalidPassword() {
        // Arrange
        createUserRequest.setPassword("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.sendVerificationOTP(createUserRequest));
    }

    @Test
    void sendVerificationOTP_NullPassword() {
        // Arrange
        createUserRequest.setPassword(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.sendVerificationOTP(createUserRequest));
    }

    @Test
    void register_Success() {
        // Arrange
        when(pendingUserRepository.findByEmail(registerUserRequest.getEmail()))
                .thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        CreatedUserResponse response = userService.register(registerUserRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("jwt-token", response.getJwtToken()),
                () -> assertEquals("test@example.com", response.getUser().getEmail())
        );

        verify(pendingUserRepository).findByEmail(registerUserRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateToken(any(User.class));
        verify(pendingUserRepository).delete(pendingUser);
    }

    @Test
    void register_PendingUserNotFound() {
        // Arrange
        when(pendingUserRepository.findByEmail(registerUserRequest.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.register(registerUserRequest)
        );

        assertNotNull(exception);
        verify(pendingUserRepository).findByEmail(registerUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_InvalidOTP() {
        // Arrange
        pendingUser.setOtp("654321");
        when(pendingUserRepository.findByEmail(registerUserRequest.getEmail()))
                .thenReturn(Optional.of(pendingUser));

        // Act & Assert
        InvalidOtpException exception = assertThrows(
                InvalidOtpException.class,
                () -> userService.register(registerUserRequest)
        );

        assertNotNull(exception);
        verify(pendingUserRepository).findByEmail(registerUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void uploadFile_Success() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        String cloudinaryUrl = "https://cloudinary.com/image.jpg";
        when(cloudinaryService.uploadFile(file)).thenReturn(cloudinaryUrl);

        // Act
        UploadResponse response = userService.uploadFile(file);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals(cloudinaryUrl, response.getCloudinaryUrl())
        );

        verify(cloudinaryService).uploadFile(file);
    }

    @Test
    void uploadFile_IOException() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(cloudinaryService.uploadFile(file)).thenThrow(new IOException("Upload failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.uploadFile(file)
        );

        assertNotNull(exception);
        verify(cloudinaryService).uploadFile(file);
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtTokenUtil.generateToken(user)).thenReturn("jwt-token");

        // Act
        LoginResponse response = userService.login(loginRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("jwt-token", response.getToken()),
                () -> assertEquals("test@example.com", response.getUser().getEmail())
        );

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
        verify(jwtTokenUtil).generateToken(user);
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.login(loginRequest)
        );

        assertNotNull(exception);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(loginRequest.getEmail());
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(false);

        // Act & Assert
        InvalidPasswordException exception = assertThrows(
                InvalidPasswordException.class,
                () -> userService.login(loginRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void login_InvalidRole() {
        // Arrange
        loginRequest.setRole(Role.ADMIN);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        // Act & Assert
        InvalidRoleException exception = assertThrows(
                InvalidRoleException.class,
                () -> userService.login(loginRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void login_InactiveUser() {
        // Arrange
        user.setActive(false);
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())).thenReturn(true);

        // Act & Assert
        IsNotActiveException exception = assertThrows(IsNotActiveException.class, () -> userService.login(loginRequest));
        assertNotNull(exception);
        verify(userRepository).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), user.getPassword());
    }

    @Test
    void updateProfile_Success() {
        // Arrange
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenUtil.generateToken(user)).thenReturn("jwt-token");

        // Act
        UpdateUserProfileResponse response = userService.updateProfile(updateUserProfileRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("jwt-token", response.getToken())
        );

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(user);
        verify(jwtTokenUtil).generateToken(user);
    }

    @Test
    void updateProfile_NoAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> userService.updateProfile(updateUserProfileRequest));
    }

    @Test
    void updateProfile_UserNotFound() {
        // Arrange
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateProfile(updateUserProfileRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void updateProfile_InactiveUser() {
        // Arrange
        user.setActive(false);
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Act & Assert
        IsNotActiveException exception = assertThrows(
                IsNotActiveException.class,
                () -> userService.updateProfile(updateUserProfileRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        when(userRepository.findByEmail(changePasswordRequest.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(changePasswordRequest.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        ResetPasswordResponse response = userService.resetPassword(changePasswordRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("test@example.com", response.getEmail()),
                () -> assertEquals("Password reset successful", response.getMessage())
        );

        verify(otpService).verifyOtp(changePasswordRequest.getEmail(), changePasswordRequest.getOtp());
        verify(userRepository).findByEmail(changePasswordRequest.getEmail());
        verify(passwordEncoder).encode(changePasswordRequest.getNewPassword());
        verify(userRepository).save(user);
        verify(otpService).deleteOtp(changePasswordRequest.getEmail(), changePasswordRequest.getOtp());
    }

    @Test
    void resetPassword_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(changePasswordRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.resetPassword(changePasswordRequest)
        );

        assertNotNull(exception);
        verify(otpService).verifyOtp(changePasswordRequest.getEmail(), changePasswordRequest.getOtp());
        verify(userRepository).findByEmail(changePasswordRequest.getEmail());
    }

    @Test
    void sendResetOtp_Success() {
        // Arrange
        when(userRepository.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.of(user));
        when(otpService.sendOtp(resetPasswordRequest.getEmail()))
                .thenReturn(new OTPResponse("123456", "test@example.com", "OTP sent Successfully"));

        // Act
        ResetPasswordResponse response = userService.sendResetOtp(resetPasswordRequest);

        // Assert
        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("test@example.com", response.getEmail()),
                () -> assertEquals("OTP sent Successfully", response.getMessage())
        );

        verify(userRepository).findByEmail(resetPasswordRequest.getEmail());
        verify(otpService).sendOtp(resetPasswordRequest.getEmail());
    }

    @Test
    void sendResetOtp_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.sendResetOtp(resetPasswordRequest)
        );

        assertNotNull(exception);
        verify(userRepository).findByEmail(resetPasswordRequest.getEmail());
    }

    @Test
    void findUserById_Success() {
        String userId = "1";
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        FoundResponse response = userService.findUserById(userId);

        assertAll(() -> assertNotNull(response), () -> assertEquals(userId, response.getId()));

        verify(userRepository).findById(userId);
    }

    @Test
    void findUserById_UserNotFound() {
        String userId = "1";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.findUserById(userId));

        assertNotNull(exception);
        verify(userRepository).findById(userId);
    }

    @Test
    void voiceSignup_Success() throws IOException {
        // Fix: Create proper EmbeddingResponse mock
        Embedding embedding1 = new Embedding();
        embedding1.setVoicePrintList(Arrays.asList(1.0, 2.0, 3.0));
        EmbeddingResponse embeddingResponse = new EmbeddingResponse();
        embeddingResponse.setEmbedding(embedding1);

        when(userRepository.findByEmail(voiceSignupRequest.getEmail())).thenReturn(Optional.empty());
        when(voiceAuthenticationService.extractVoiceFeatures(voiceSignupRequest.getVoiceSample()))
                .thenReturn(embeddingResponse);
        when(embeddingRepository.save(any(Embedding.class))).thenReturn(embedding);
        when(voiceAuthenticationService.generateSecurePassword()).thenReturn("securePassword");
        when(passwordEncoder.encode("securePassword")).thenReturn("encodedSecurePassword");
        when(otpService.sendOtp(voiceSignupRequest.getEmail()))
                .thenReturn(new OTPResponse("123456", "test@example.com", "OTP sent"));
        when(pendingUserRepository.save(any(PendingUser.class))).thenReturn(pendingUser);

        VoiceRegistrationResponse response = userService.voiceSignup(voiceSignupRequest);

        assertAll(() -> assertNotNull(response), () -> assertEquals("Voice signup initiated. Please verify OTP to complete registration.", response.getMessage()), () -> assertEquals("test@example.com", response.getEmail()));

        verify(userRepository).findByEmail(voiceSignupRequest.getEmail());
        verify(voiceAuthenticationService).extractVoiceFeatures(voiceSignupRequest.getVoiceSample());
        verify(embeddingRepository).save(any(Embedding.class));
        verify(otpService).sendOtp(voiceSignupRequest.getEmail());
        verify(pendingUserRepository).save(any(PendingUser.class));
    }

    @Test
    void voiceSignup_EmailAlreadyExists() throws IOException {
        when(userRepository.findByEmail(voiceSignupRequest.getEmail())).thenReturn(Optional.of(user));

        AlreadyExistsException exception = assertThrows(AlreadyExistsException.class, () -> userService.voiceSignup(voiceSignupRequest));

        assertNotNull(exception);
        verify(userRepository).findByEmail(voiceSignupRequest.getEmail());
        verify(voiceAuthenticationService, never()).extractVoiceFeatures(any());
    }

    @Test
    void voiceSignup_MissingFields() {
        voiceSignupRequest.setEmail("");

        assertThrows(IllegalArgumentException.class, () -> userService.voiceSignup(voiceSignupRequest));
    }

    @Test
    void voiceSignup_VoiceProcessingFailed() throws IOException {
        when(userRepository.findByEmail(voiceSignupRequest.getEmail())).thenReturn(Optional.empty());
        when(voiceAuthenticationService.extractVoiceFeatures(voiceSignupRequest.getVoiceSample()))
                .thenThrow(new IOException("Voice processing failed"));

        VoiceProcessingFailedException exception = assertThrows(VoiceProcessingFailedException.class, () -> userService.voiceSignup(voiceSignupRequest));

        assertNotNull(exception);
        verify(voiceAuthenticationService).extractVoiceFeatures(voiceSignupRequest.getVoiceSample());
    }

    @Test
    void voiceSignup_NullVoiceSample() {
        voiceSignupRequest.setVoiceSample(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userService.voiceSignup(voiceSignupRequest));
        assertNotNull(exception);
    }

    @Test
    void completeVoiceRegistration_Success() {
        when(pendingUserRepository.findByEmail(completeVoiceRegistrationRequest.getEmail()))
                .thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenUtil.generateToken(any(User.class))).thenReturn("jwt-token");

        CreatedUserResponse response = userService.completeVoiceRegistration(completeVoiceRegistrationRequest);

        assertAll(() -> assertNotNull(response), () -> assertEquals("jwt-token", response.getJwtToken()), () -> assertEquals("test@example.com", response.getUser().getEmail())
        );

        verify(pendingUserRepository).findByEmail(completeVoiceRegistrationRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenUtil).generateToken(any(User.class));
        verify(pendingUserRepository).delete(pendingUser);
    }

    @Test
    void completeVoiceRegistration_PendingUserNotFound() {
        when(pendingUserRepository.findByEmail(completeVoiceRegistrationRequest.getEmail()))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.completeVoiceRegistration(completeVoiceRegistrationRequest));

        assertNotNull(exception);
        verify(pendingUserRepository).findByEmail(completeVoiceRegistrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void completeVoiceRegistration_InvalidOTP() {
        pendingUser.setOtp("wrong-otp");
        when(pendingUserRepository.findByEmail(completeVoiceRegistrationRequest.getEmail()))
                .thenReturn(Optional.of(pendingUser));

        InvalidOtpException exception = assertThrows(InvalidOtpException.class, () -> userService.completeVoiceRegistration(completeVoiceRegistrationRequest));

        assertNotNull(exception);
        verify(pendingUserRepository).findByEmail(completeVoiceRegistrationRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void voiceLogin_Success() throws IOException {
        user.setVoiceAuthEnabled(true);
        user.setVoicePrint("voice-print-data");

        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(voiceAuthenticationService.verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint()))
                .thenReturn(true);
        when(jwtTokenUtil.generateToken(user)).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        LoginResponse response = userService.voiceLogin(voiceLoginRequest);

        assertAll(() -> assertNotNull(response), () -> assertEquals("jwt-token", response.getToken()), () -> assertEquals("test@example.com", response.getUser().getEmail()));

        verify(userRepository).findByEmail(voiceLoginRequest.getEmail());
        verify(voiceAuthenticationService).verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint());
        verify(jwtTokenUtil).generateToken(user);
    }

    @Test
    void voiceLogin_UserNotFound() throws IOException {
        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.voiceLogin(voiceLoginRequest));

        assertNotNull(exception);
        verify(userRepository).findByEmail(voiceLoginRequest.getEmail());
        verify(voiceAuthenticationService, never()).verifyVoice(any(), any());
    }

    @Test
    void voiceLogin_VoiceAuthNotEnabled() throws IOException {
        user.setVoiceAuthEnabled(false);
        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.of(user));

        VoiceAuthenticationException exception = assertThrows(VoiceAuthenticationException.class, () -> userService.voiceLogin(voiceLoginRequest));

        assertNotNull(exception);
        verify(userRepository).findByEmail(voiceLoginRequest.getEmail());
        verify(voiceAuthenticationService, never()).verifyVoice(any(), any());
    }

    @Test
    void voiceLogin_VoiceDoesNotMatch() throws IOException {
        user.setVoiceAuthEnabled(true);
        user.setVoicePrint("voice-print-data");

        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(voiceAuthenticationService.verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint()))
                .thenReturn(false);

        VoiceDoesNotMatchException exception = assertThrows(VoiceDoesNotMatchException.class, () -> userService.voiceLogin(voiceLoginRequest));

        assertNotNull(exception);
        verify(voiceAuthenticationService).verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint());
    }

    @Test
    void voiceLogin_VoiceProcessingFailed() throws IOException {
        user.setVoiceAuthEnabled(true);
        user.setVoicePrint("voice-print-data");

        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.of(user));
        when(voiceAuthenticationService.verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint()))
                .thenThrow(new IOException("Voice processing failed"));

        VoiceProcessingFailedException exception = assertThrows(VoiceProcessingFailedException.class, () -> userService.voiceLogin(voiceLoginRequest)
        );

        assertNotNull(exception);
        verify(voiceAuthenticationService).verifyVoice(voiceLoginRequest.getVoiceSample(), user.getVoicePrint());
    }

    @Test
    void voiceLogin_InactiveUser() throws IOException {
        user.setActive(false);
        user.setVoiceAuthEnabled(true);
        user.setVoicePrint("voice-print-data");

        when(userRepository.findByEmail(voiceLoginRequest.getEmail())).thenReturn(Optional.of(user));

        InactiveUserException exception = assertThrows(InactiveUserException.class, () -> userService.voiceLogin(voiceLoginRequest));
        assertNotNull(exception);
        verify(userRepository).findByEmail(voiceLoginRequest.getEmail());
        verify(voiceAuthenticationService, never()).verifyVoice(any(), any());
    }

    @Test
    void enableVoiceAuthentication_Success() throws IOException {
        setupAuthentication();

        // Fix: Create proper EmbeddingResponse mock
        Embedding embedding1 = new Embedding();
        embedding1.setVoicePrintList(Arrays.asList(1.0, 2.0, 3.0));
        EmbeddingResponse embeddingResponse = new EmbeddingResponse();
        embeddingResponse.setEmbedding(embedding1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(voiceAuthenticationService.extractVoiceFeatures(enableVoiceAuthRequest.getVoiceSample()))
                .thenReturn(embeddingResponse);
        when(embeddingRepository.save(any(Embedding.class))).thenReturn(embedding);
        when(userRepository.save(any(User.class))).thenReturn(user);

        VoiceAuthResponse response = userService.enableVoiceAuthentication(enableVoiceAuthRequest);

        assertAll(() -> assertNotNull(response), () -> assertEquals("Voice authentication enabled successfully", response.getMessage()), () -> assertEquals("test@example.com", response.getEmail()));

        verify(voiceAuthenticationService).extractVoiceFeatures(enableVoiceAuthRequest.getVoiceSample());
        verify(embeddingRepository).save(any(Embedding.class));
        verify(userRepository).save(user);
        assertTrue(user.isVoiceAuthEnabled());
    }

    @Test
    void enableVoiceAuthentication_UserNotFound() throws IOException {
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.enableVoiceAuthentication(enableVoiceAuthRequest));

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
        verify(voiceAuthenticationService, never()).extractVoiceFeatures(any());
    }

    @Test
    void enableVoiceAuthentication_VoiceProcessingFailed() throws IOException {
        setupAuthentication();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(voiceAuthenticationService.extractVoiceFeatures(enableVoiceAuthRequest.getVoiceSample()))
                .thenThrow(new IOException("Voice processing failed"));

        VoiceProcessingFailedException exception = assertThrows(VoiceProcessingFailedException.class, () -> userService.enableVoiceAuthentication(enableVoiceAuthRequest));

        assertNotNull(exception);
        verify(voiceAuthenticationService).extractVoiceFeatures(enableVoiceAuthRequest.getVoiceSample());
    }

    @Test
    void enableVoiceAuthentication_AlreadyEnabled() throws IOException {
        setupAuthentication();
        user.setVoiceAuthEnabled(true);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        VoiceAlreadyEnabledException exception = assertThrows(VoiceAlreadyEnabledException.class,() -> userService.enableVoiceAuthentication(enableVoiceAuthRequest));

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
        verify(voiceAuthenticationService, never()).extractVoiceFeatures(any());
    }

    @Test
    void disableVoiceAuthentication_Success() {
        setupAuthentication();
        user.setVoiceAuthEnabled(true);
        user.setVoicePrint("voice-print-data");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        VoiceAuthResponse response = userService.disableVoiceAuthentication();

        assertAll(() -> assertNotNull(response), () -> assertEquals("Voice authentication disabled successfully", response.getMessage()), () -> assertEquals("test@example.com", response.getEmail()));

        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository).save(user);
        assertNull(user.getVoicePrint());
        assertFalse(user.isVoiceAuthEnabled());
    }

    @Test
    void disableVoiceAuthentication_UserNotFound() {
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.disableVoiceAuthentication());

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void disableVoiceAuthentication_AlreadyDisabled() {
        setupAuthentication();
        user.setVoiceAuthEnabled(false);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> userService.disableVoiceAuthentication());

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void logout_Success() {
        setupAuthentication();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenUtil.extractTokenFromContext()).thenReturn("jwt-token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        LogoutResponse response = userService.logout();

        assertAll(() -> assertNotNull(response), () -> assertEquals("Logout successful", response.getMessage()), () -> assertEquals("test@example.com", response.getEmail()));

        verify(userRepository).findByEmail("test@example.com");
        verify(jwtTokenUtil).extractTokenFromContext();
        verify(tokenBlacklistService).blacklistToken("jwt-token");
        verify(userRepository).save(user);
    }

    @Test
    void logout_UserNotFound() {
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.logout());

        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
        verify(tokenBlacklistService, never()).blacklistToken(any());
    }

    @Test
    void logout_TokenExtractionFailed() {
        setupAuthentication();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenUtil.extractTokenFromContext()).thenThrow(new RuntimeException("Token extraction failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.logout());

        assertNotNull(exception);
        assertEquals("Token extraction failed", exception.getMessage());
        verify(jwtTokenUtil).extractTokenFromContext();
    }

    @Test
    void logoutFromAllDevices_Success() {
        setupAuthentication();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        LogoutResponse response = userService.logoutFromAllDevices();

        assertAll(() -> assertNotNull(response), () -> assertEquals("Logged out from all devices successfully", response.getMessage()), () -> assertEquals("test@example.com", response.getEmail()));
        verify(userRepository).findByEmail("test@example.com");
        verify(tokenBlacklistService).blacklistAllUserTokens(user.getId());
        verify(userRepository).save(user);
    }

    @Test
    void logoutFromAllDevices_UserNotFound() {
        setupAuthentication();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> userService.logoutFromAllDevices());
        assertNotNull(exception);
        verify(userRepository).findByEmail("test@example.com");
        verify(tokenBlacklistService, never()).blacklistAllUserTokens(any());
    }

    @Test
    void getAuthentication_NoAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        NoAuthenticatedUserException exception = assertThrows(NoAuthenticatedUserException.class, () -> userService.logout());
        assertNotNull(exception);
    }

    @Test
    void getAuthentication_NotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        NoAuthenticatedUserException exception = assertThrows(NoAuthenticatedUserException.class, () -> userService.logout());
        assertNotNull(exception);
    }

    @Test
    void sendVerificationOTP_OTPServiceException() {
        when(userRepository.findByEmail(createUserRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
        when(otpService.sendOtp(createUserRequest.getEmail()))
                .thenThrow(new RuntimeException("OTP service unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.sendVerificationOTP(createUserRequest));
        assertNotNull(exception);
        assertEquals("OTP service unavailable", exception.getMessage());
        verify(otpService).sendOtp(createUserRequest.getEmail());
    }

    @Test
    void register_UserCreationFailed() {
        when(pendingUserRepository.findByEmail(registerUserRequest.getEmail()))
                .thenReturn(Optional.of(pendingUser));
        when(userRepository.save(any(User.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.register(registerUserRequest));
        assertNotNull(exception);
        assertEquals("Database connection failed", exception.getMessage());
        verify(userRepository).save(any(User.class));
    }

    private void setupAuthentication() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");
        SecurityContextHolder.setContext(securityContext);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}