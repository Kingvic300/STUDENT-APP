package com.izabi.util;


import java.time.LocalDateTime;
import java.util.Random;

public class OTPGenerator {

    public static String generateOTP() {
        Random random = new Random();
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }
    public static LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plusMinutes(2);
    }
    public static LocalDateTime getCurrentTime() {
        return LocalDateTime.now();
    }
}
