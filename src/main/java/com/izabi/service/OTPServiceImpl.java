package com.izabi.service;

import com.izabi.data.model.OTP;
import com.izabi.data.repository.OTPRepository;
import com.izabi.dto.response.OTPResponse;
import com.izabi.exception.OTPCannotBeBlankException;
import com.izabi.mapper.OTPMapper;
import com.izabi.util.OTPGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OTPServiceImpl implements OTPService {

    private final OTPRepository otpRepository;
    private final EmailService emailService;

    @Override
    public OTPResponse sendOtp(String email) {
        String generatedOtp;
        try {
            generatedOtp = OTPGenerator.generateOTP();
            if (generatedOtp.isBlank()) {
                throw new OTPCannotBeBlankException("Generated OTP cannot be null or blank");
            }
            OTP otp = new OTP();
            otp.setEmail(email);
            otp.setOtp(generatedOtp);
            otp.setExpiresAt(OTPGenerator.calculateExpiryTime());
            try {
                emailService.sendEmail(email, generatedOtp);
            }catch (Exception e) {
                throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
            }
            otp.setUsed(true);
            otpRepository.save(otp);

        }catch (Exception e) {
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
        return OTPMapper.mapToOTPResponse(generatedOtp, "OTP sent successfully", email);
    }

    @Override
    public OTPResponse sendResetPasswordOtp(String email) {
        String generatedOtp = OTPGenerator.generateOTP();
        try {
            if (generatedOtp.isBlank()) {
                throw new OTPCannotBeBlankException("Generated OTP cannot be null or blank");
            }
            OTP otp = new OTP();
            otp.setEmail(email);
            otp.setOtp(generatedOtp);
            otp.setExpiresAt(OTPGenerator.calculateExpiryTime());
            try {
                emailService.sendResetPasswordEmail(email, generatedOtp);
            }catch (Exception e) {
                throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
            }
            otp.setUsed(true);
            otpRepository.save(otp);

        }catch (Exception e) {
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
        return OTPMapper.mapToOTPResponse(generatedOtp, "OTP sent successfully",email);
    }
    @Override
    public OTPResponse verifyOtp(String email, String otp) {
        OTP otpEntity = otpRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP or email"));

        if (otpEntity.isUsed()) {
            throw new RuntimeException("OTP has already been used");
        }

        if (otpEntity.getExpiresAt().isBefore(OTPGenerator.getCurrentTime())) {
            throw new RuntimeException("OTP has expired");
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);

        return OTPMapper.mapToOTPResponse(otp, "OTP verified successfully", email);
    }
    @Override
    public OTPResponse deleteOtp(String email, String otp) {
        OTP otpEntity = otpRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP or email"));

        boolean isUsed = otpEntity.isUsed();
        boolean isExpired = otpEntity.getExpiresAt().isBefore(OTPGenerator.getCurrentTime());

        if (!isUsed && !isExpired) {
            throw new RuntimeException("OTP is still valid and unused. Cannot delete.");
        }

        otpRepository.delete(otpEntity);

        return OTPMapper.mapToOTPResponse(otp, "OTP deleted successfully",email);
    }

}
