package com.example.hhvolgograd.web.service.auth;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistence.db.service.CashService;
import com.example.hhvolgograd.persistence.grid.service.HazelcastFactory;
import com.example.hhvolgograd.persistence.grid.service.HazelcastMapService;
import com.example.hhvolgograd.web.security.Scope;
import com.example.hhvolgograd.web.security.token.TokenGenerator;
import com.example.hhvolgograd.web.security.token.TokenParameter;
import lombok.val;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.hhvolgograd.persistence.grid.service.HazelcastMapServiceType.OTP_SERVICE;
import static java.lang.String.format;

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
        requireOtpIsCorrect(email, otp);

        val user = cashService
                .findUserByEmail(email)
                .orElseThrow(() -> new NotRegisteringUserException(
                        format("'%s' user was not found among registered users.", email))
                );
        val id = user.getId().toString();
        val scope = Arrays.stream(Scope.values())
                .map(Scope::getValue)
                .collect(Collectors.joining(", "))
                + ", " + id;


        return tokenGenerator.generate(
                TokenParameter
                        .create()
                        .withEmail(email)
                        .withUserId(id)
                        .withScope(scope)
                        .build()
        );
    }

    private void requireOtpIsCorrect(String email, String otp) {
        if (!otpService.read(email).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }
}
