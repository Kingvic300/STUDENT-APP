package com.izabi.util;

import com.izabi.exception.InvalidEmailRegexException;

public class EmailVerification {

    public static String emailVerification(String email) {
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if(email.matches(regex)){
            return email;
        }
        throw new InvalidEmailRegexException("Invalid email format: " + email);
    }
}
