package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.KeepingUserService;
import com.example.hhvolgograd.persistance.grid.service.KeepingUserServiceImpl;
import com.example.hhvolgograd.persistance.grid.service.OtpService;
import com.example.hhvolgograd.persistance.grid.service.OtpServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrationServiceImplTest {

    private CashService cashService;
    private OtpService otpService;
    private KeepingUserService keepingUserService;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        keepingUserService = mock(KeepingUserServiceImpl.class);
        otpService = mock(OtpServiceImpl.class);
        mailService = mock(MailService.class);
        cashService = mock(CashService.class);
    }

    @AfterEach
    void tearDown() {
        keepingUserService = null;
        otpService = null;
        mailService = null;
        cashService = null;
    }

    @Test
    void register_alreadyRegisteredEmail_throws() throws JsonProcessingException {
        val user = mock(User.class);
        val userEmail = randomStringWithNonZeroLength();
        val userJson = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(user.getEmail())
                .thenReturn(userEmail);
        when(user.toJson())
                .thenReturn(userJson);
        doNothing()
                .when(keepingUserService)
                .save(anyString(), anyString());
        when(otpService.save(anyString()))
                .thenReturn(RandomStringUtils.random(32));
        doNothing()
                .when(mailService)
                .send(anyString(), anyString());
        when(cashService.existsUserByEmail(anyString()))
                .thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> registrationService.register(user));
    }

    @Test
    void register_notRegisteredYetEmail_ok() throws JsonProcessingException {
        val user = mock(User.class);
        val userEmail = randomStringWithNonZeroLength();
        val userJson = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(user.getEmail())
                .thenReturn(userEmail);
        when(user.toJson())
                .thenReturn(userJson);
        doNothing()
                .when(keepingUserService)
                .save(anyString(), anyString());
        when(otpService.save(anyString()))
                .thenReturn(RandomStringUtils.random(32));
        doNothing()
                .when(mailService)
                .send(anyString(), anyString());
        when(cashService.existsUserByEmail(anyString()))
                .thenReturn(false);

        assertDoesNotThrow(() -> registrationService.register(user));
        verify(keepingUserService, times(1)).save(anyString(), anyString());
        verify(otpService, times(1)).save(anyString());
        verify(mailService, times(1)).send(anyString(), anyString());
        verify(cashService, times(1)).existsUserByEmail(anyString());
    }

    @Test
    void confirmRegistration_inputEmailIsNull_throws() {
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        assertThrows(NullPointerException.class, () -> registrationService.confirmRegistration(null, otp));
    }

    @Test
    void confirmRegistration_inputOtpIsNull_throws() {
        val email = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        assertThrows(NullPointerException.class, () -> registrationService.confirmRegistration(email, null));
    }

    @Test
    void confirmRegistration_otpNotFound_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(otpService.findOtpByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp));
    }

    @Test
    void confirmRegistration_inputOtpDiffersFromStoredOtp_throws() {
        val email = randomStringWithNonZeroLength();
        val otp1 = randomStringWithNonZeroLength();
        val otp2 = differentOtp(otp1);
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(otpService.findOtpByEmail(anyString()))
                .thenReturn(Optional.of(otp2));

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp1));
    }

    @Test
    void confirmRegistration_userNotFound_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(otpService.findOtpByEmail(anyString()))
                .thenReturn(Optional.of(otp));
        when(keepingUserService.findUserByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp));
    }

    @Test
    void confirmRegistration_ok() {
        val email = "a@a.com";
        val user = mock(User.class);
        val userJson = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        try (final MockedStatic<User> userStatic = mockStatic(User.class)) {
            when(otpService.findOtpByEmail(anyString()))
                    .thenReturn(Optional.of(otp));
            when(keepingUserService.findUserByEmail(anyString()))
                    .thenReturn(Optional.of(userJson));
            when(cashService.save(any())).thenReturn(user);
            userStatic.when(() -> User.fromJson(anyString())).thenReturn(user);
            registrationService.confirmRegistration(email, otp);

            verify(otpService, times(1)).findOtpByEmail(anyString());
            verify(keepingUserService, times(1)).findUserByEmail(anyString());
            verify(cashService, times(1)).save(any());
            userStatic.verify(() -> User.fromJson(anyString()), times(1));
        }

    }

    private String differentOtp(String otp1) {
        var otp2 = randomStringWithNonZeroLength();

        while (otp2.equals(otp1)) {
            otp2 = randomStringWithNonZeroLength();
        }
        return otp2;
    }

    private String randomStringWithNonZeroLength() {
        return RandomStringUtils.random(1 + new Random().nextInt(99));
    }

}