package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.service.CashService;
import com.example.hhvolgograd.persistence.grid.service.HazelcastFactory;
import com.example.hhvolgograd.persistence.grid.service.HazelcastMapService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.hhvolgograd.persistence.grid.service.HazelcastMapServiceType.KEEPING_USER_SERVICE;
import static com.example.hhvolgograd.persistence.grid.service.HazelcastMapServiceType.OTP_SERVICE;
import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final CashService cashService;
    private final HazelcastMapService otpService;
    private final HazelcastMapService keepingUserService;
    private final MailService mailService;

    public RegistrationServiceImpl(CashService cashService, HazelcastFactory factory, MailService mailService) {
        this.cashService = cashService;
        this.otpService = factory.createInstance(OTP_SERVICE);
        this.keepingUserService = factory.createInstance(KEEPING_USER_SERVICE);
        this.mailService = mailService;
    }

    @SneakyThrows
    public void register(User user) {
        val email = user.getEmail();
        val userJson = user.toJson();
        val otp = UUID.randomUUID().toString();

        cashService.requireNoSuchEmailIsRegistered(email);
        log.debug("User to send:\n{}", userJson);
        keepingUserService.save(email, userJson);

        otpService.save(email, otp);
        log.debug("otp '{}'", otp);
        mailService.send(email, otp);
    }

    @Override
    public void confirmRegistration(String email, String otp) {
        requireNonNull(email, "Email could not be null");
        requireNonNull(otp, "Otp could not be null");
        requireOtpIsCorrect(email, otp);
        val user = User.fromJson(keepingUserService.read(email));
        cashService.save(user);
    }

    private void requireOtpIsCorrect(String email, String otp) {
        if (!otpService.read(email).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }

}
