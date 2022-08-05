package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.HazelcastFactory;
import com.example.hhvolgograd.persistance.grid.service.HazelcastMapService;
import com.example.hhvolgograd.web.security.Scope;
import com.example.hhvolgograd.web.security.TokenGenerator;
import com.example.hhvolgograd.web.security.TokenParameter;
import lombok.val;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.hhvolgograd.persistance.grid.service.HazelcastMapServiceType.OTP_SERVICE;

@Service
public class LoginServiceImpl implements LoginService {

    private final CashService cashService;
    private final HazelcastMapService otpService;
    private final MailService mailService;
    private final TokenGenerator tokenGenerator;

    public LoginServiceImpl(
            CashService cashService,
            HazelcastFactory factory,
            MailService mailService,
            TokenGenerator tokenGenerator
    ) {
        this.cashService = cashService;
        this.otpService = factory.createInstance(OTP_SERVICE);
        this.mailService = mailService;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public void login(String email) {
        cashService.requireEmailIsRegistered(email);

        val otp = UUID.randomUUID().toString();

        otpService.save(email, otp);
        mailService.send(email, otp);
    }

    @Override
    public String token(String email, String otp) {
        checkIfOtpIsCorrect(email, otp);

        val user = cashService.findUserByEmail(email);
        val id = user.getId().toString();
        val scope = Arrays.stream(Scope.values())
                .map(Scope::getValue)
                .collect(Collectors.joining(", "));

        return tokenGenerator.generate(
                TokenParameter
                        .create()
                        .email(email)
                        .userId(id)
                        .scope(scope)
                        .build()
        );
    }

    private void checkIfOtpIsCorrect(String email, String otp) {
        if (!otpService.read(email).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }
}
