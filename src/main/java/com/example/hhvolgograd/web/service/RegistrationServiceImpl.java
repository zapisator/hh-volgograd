package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.KeepingUserService;
import com.example.hhvolgograd.persistance.grid.service.OtpService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@Service
@AllArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final CashService cashService;
    private final OtpService otpService;
    private final KeepingUserService keepingUserService;
    private final MailService mailService;

    @SneakyThrows
    public void register(User user) {
        val email = user.getEmail();
        val userJson = user.toJson();

        checkNoSuchEmailIsRegistered(email);
        log.debug("User to send:\n{}", userJson);
        keepingUserService.save(email, userJson);

        val otp = otpService.save(email);
        log.debug("otp '{}'", otp);
        mailService.send(email, otp);
    }

    @Override
    public void confirmRegistration(String email, String otp) {
        requireNonNull(email, "Email could not be null");
        requireNonNull(otp, "Otp could not be null");
        checkIfOtpIsCorrect(email, otp);
        val user = getUserOrThrow(email);
        cashService.save(user);
    }

    private void checkIfOtpIsCorrect(String email, String otp) {
        if (!otpService
                .findOtpByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(format(
                        "User with email '%s' was not found in the registration storage",
                        email))
                ).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }

    private User getUserOrThrow(String email) {
        return keepingUserService
                .findUserByEmail(email)
                .map(userJson -> {
                    try {
                        return new ObjectMapper().readValue(userJson, User.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(
                        () -> new NotRegisteringUserException(format(
                                "User with email '%s' is not registering, "
                                        + "or too much time spent after register form was sent. Try again to register.",
                                email
                        ))
                );
    }

    private void checkNoSuchEmailIsRegistered(String email) {
        if (cashService.existsUserByEmail(email)) {
            throw new DuplicateKeyException(format("User with '%s' email has already registered.", email));
        }
    }
}
