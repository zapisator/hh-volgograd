package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.OtpService;
import com.example.hhvolgograd.web.security.TokenGenerator;
import com.example.hhvolgograd.web.security.TokenParameter;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.example.hhvolgograd.web.security.Scope.PHONES_READ;
import static com.example.hhvolgograd.web.security.Scope.PHONES_WRITE;
import static com.example.hhvolgograd.web.security.Scope.PROFILE_READ;
import static com.example.hhvolgograd.web.security.Scope.USERINFO_READ;
import static com.example.hhvolgograd.web.security.Scope.USERINFO_WRITE;

@Service
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final CashService cashService;
    private final OtpService otpService;
    private final MailService mailService;
    private final TokenGenerator tokenGenerator;

    @Override
    public void login(String email) {
        cashService.checkIfEmailIsRegistered(email);
        val otp = otpService.save(email);
        mailService.send(email, otp);
    }

    @Override
    public String token(String email, String otp) {
        checkIfOtpIsCorrect(email, otp);

        val user = cashService.findUserByEmail(email);
        val id = user.getId().toString();
        val scope = String.join(", ",
                USERINFO_READ,
                USERINFO_WRITE,
                PROFILE_READ,
                PHONES_READ,
                PHONES_WRITE
        );

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
        if (!otpService.getOtpOrThrow(email).equals(otp)) {
            throw new UsernameNotFoundException("User email or password are incorrect.");
        }
    }
}
