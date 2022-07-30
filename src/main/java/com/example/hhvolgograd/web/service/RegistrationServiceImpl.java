package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.KeepingUserService;
import com.example.hhvolgograd.persistance.grid.service.OtpService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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

        cashService.checkIfNoSuchEmailIsRegistered(email);
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
        val user = keepingUserService.getUserOrThrow(email);
        cashService.save(user);
    }

    private void checkIfOtpIsCorrect(String email, String otp) {
        if (!otpService.getOtpOrThrow(email).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }

}
