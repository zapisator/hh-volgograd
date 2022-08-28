package com.example.hhvolgograd.web.service.auth;

import com.example.hhvolgograd.persistence.db.model.User;

public interface RegistrationService {

    void register(User user);

    void confirmRegistration(String email, String otp);
}
