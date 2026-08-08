package com.mandateguardian.identity.application;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String subject) {
        super("No active user profile exists for subject: " + subject);
    }
}
