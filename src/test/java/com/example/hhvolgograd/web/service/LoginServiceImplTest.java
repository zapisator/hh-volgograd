package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.exception.NotRegisteringUserException;
import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.service.CashService;
import com.example.hhvolgograd.persistence.grid.service.HazelcastFactory;
import com.example.hhvolgograd.persistence.grid.service.HazelcastMapService;
import com.example.hhvolgograd.web.security.TokenGenerator;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceImplTest {

    private CashService cashService;
    private HazelcastFactory hazelcastFactory;
    private HazelcastMapService otpService;
    private MailService mailService;
    private TokenGenerator tokenGenerator;

    @BeforeEach
    void setUp() {
        hazelcastFactory = mock(HazelcastFactory.class);
        otpService = mock(HazelcastMapService.class);
        mailService = mock(MailService.class);
        cashService = mock(CashService.class);
        tokenGenerator = mock(TokenGenerator.class);

        when(hazelcastFactory.createInstance(any()))
                .thenReturn(otpService);
    }

    @AfterEach
    void tearDown() {
        cashService = null;
        hazelcastFactory = null;
        otpService = null;
        mailService = null;
        tokenGenerator = null;
    }

    @Test
    void login_emailIsNotRegistered_throws() {
        val email = randomStringWithNonZeroLength();
        val exception = new NotRegisteringUserException(format("User with '%s' email has no been registered.", email));
        val loginService = new LoginServiceImpl(cashService, hazelcastFactory, mailService, tokenGenerator);

        doThrow(exception)
                .when(cashService)
                .requireEmailIsRegistered(anyString());

        assertThrows(NotRegisteringUserException.class, () -> loginService.login(email));
        verify(cashService, times(1)).requireEmailIsRegistered(anyString());
        verify(otpService, times(0)).save(anyString(), anyString());
        verify(mailService, times(0)).send(anyString(), anyString());
    }

    @Test
    void login_emailIsRegistered_ok() {
        val email = randomStringWithNonZeroLength();
        val loginService = new LoginServiceImpl(cashService, hazelcastFactory, mailService, tokenGenerator);

        doNothing()
                .when(cashService)
                .requireEmailIsRegistered(anyString());
        doNothing()
                .when(otpService)
                .save(anyString(), anyString());
        doNothing()
                .when(mailService)
                .send(anyString(), anyString());

        assertDoesNotThrow(() -> loginService.login(email));
        verify(cashService, times(1)).requireEmailIsRegistered(anyString());
        verify(otpService, times(1)).save(anyString(), anyString());
        verify(mailService, times(1)).send(anyString(), anyString());
    }

    @Test
    void token_otpIsIncorrect_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val differentOtp = otp + ".";
        val loginService = new LoginServiceImpl(cashService, hazelcastFactory, mailService, tokenGenerator);

        when(otpService.read(anyString()))
                .thenReturn(differentOtp);

        assertThrows(UsernameNotFoundException.class, () -> loginService.token(email, otp));
        verify(otpService, times(1)).read(anyString());
        verify(cashService, times(0)).findUserByEmail(anyString());
        verify(tokenGenerator, times(0)).generate(any());
    }

    @Test
    void token_userNotFound_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val loginService = new LoginServiceImpl(cashService, hazelcastFactory, mailService, tokenGenerator);

        when(otpService.read(anyString()))
                .thenReturn(otp);
        when(cashService.findUserByEmail(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(NotRegisteringUserException.class, () -> loginService.token(email, otp));
        verify(otpService, times(1)).read(anyString());
        verify(cashService, times(1)).findUserByEmail(anyString());
        verify(tokenGenerator, times(0)).generate(any());
    }

    @Test
    void token_otpIsCorrect_ok() {
        val id = nextLong();
        val name = randomStringWithNonZeroLength();
        val age = nextInt(User.MIN_AGE, User.MAX_AGE + 1);
        val email = randomStringWithNonZeroLength();
        val user = new User(name, age, email);
        val otp = randomStringWithNonZeroLength();
        val loginService = new LoginServiceImpl(cashService, hazelcastFactory, mailService, tokenGenerator);
        val token = randomStringWithNonZeroLength();

        when(otpService.read(anyString()))
                .thenReturn(otp);
        when(cashService.findUserByEmail(anyString()))
                .thenReturn(Optional.of(user));
        when(tokenGenerator.generate(any()))
                .thenReturn(token);
        user.setId(id);

        assertDoesNotThrow(() -> loginService.token(email, otp));
        verify(otpService, times(1)).read(anyString());
        verify(cashService, times(1)).findUserByEmail(anyString());
        verify(tokenGenerator, times(1)).generate(any());
    }
}