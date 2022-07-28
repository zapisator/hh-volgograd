package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;

public interface RegistrationService {

    void register(User user);

    void confirmRegistration(String email, String otp);
}
