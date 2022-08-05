package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.TestUtils;
import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.HazelcastFactory;
import com.example.hhvolgograd.persistance.grid.service.HazelcastMapService;
import com.example.hhvolgograd.persistance.grid.service.HazelcastMapServiceType;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.stream.Stream;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static java.lang.String.format;
import static org.apache.commons.lang3.RandomUtils.nextInt;
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

class RegistrationServiceImplTest {

    private CashService cashService;
    private HazelcastFactory hazelcastFactory;
    private HazelcastMapService otpService;
    private HazelcastMapService keepingUserService;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        hazelcastFactory = mock(HazelcastFactory.class);
        keepingUserService = mock(HazelcastMapService.class);
        otpService = mock(HazelcastMapService.class);
        mailService = mock(MailService.class);
        cashService = mock(CashService.class);

        when(hazelcastFactory.createInstance(HazelcastMapServiceType.OTP_SERVICE))
                .thenReturn(otpService);
        when(hazelcastFactory.createInstance(HazelcastMapServiceType.KEEPING_USER_SERVICE))
                .thenReturn(keepingUserService);
    }

    @AfterEach
    void tearDown() {
        keepingUserService = null;
        otpService = null;
        mailService = null;
        cashService = null;
    }

    @Test
    void register_alreadyRegisteredEmail_throws() {
        val name = randomStringWithNonZeroLength();
        val age = nextInt(User.MIN_AGE, User.MAX_AGE + 1);
        val email = "a@a.com";
        val user = new User(name, age, email);
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        doNothing()
                .when(keepingUserService)
                .save(anyString(), anyString());
        doNothing()
                .when(otpService)
                .save(anyString(), anyString());
        doNothing()
                .when(mailService)
                .send(anyString(), anyString());
        doThrow(new DuplicateKeyException(format("User with '%s' email has already registered.", email)))
                .when(cashService)
                .requireNoSuchEmailIsRegistered(anyString());

        assertThrows(DuplicateKeyException.class, () -> registrationService.register(user));
    }

    @Test
    void register_notRegisteredYetEmail_ok() {
        val name = randomStringWithNonZeroLength();
        val age = nextInt(User.MIN_AGE, User.MAX_AGE + 1);
        val email = "a@a.com";
        val user = new User(name, age, email);
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        doNothing()
                .when(keepingUserService)
                .save(anyString(), anyString());
        doNothing()
                .when(otpService)
                .save(anyString(), anyString());
        doNothing()
                .when(mailService)
                .send(anyString(), anyString());
        doNothing()
                .when(cashService)
                .requireNoSuchEmailIsRegistered(anyString());

        assertDoesNotThrow(() -> registrationService.register(user));
        verify(keepingUserService, times(1)).save(anyString(), anyString());
        verify(otpService, times(1)).save(anyString(), anyString());
        verify(mailService, times(1)).send(anyString(), anyString());
        verify(cashService, times(1)).requireNoSuchEmailIsRegistered(anyString());
    }

    @Test
    void confirmRegistration_inputEmailIsNull_throws() {
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        assertThrows(NullPointerException.class, () -> registrationService.confirmRegistration(null, otp));
    }

    @Test
    void confirmRegistration_inputOtpIsNull_throws() {
        val email = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        assertThrows(NullPointerException.class, () -> registrationService.confirmRegistration(email, null));
    }

    @Test
    void confirmRegistration_otpNotFound_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        doThrow(
                new UsernameNotFoundException(format(
                        "User with email '%s' was not found in the registration storage",
                        email))
        )
                .when(otpService)
                .read(anyString());

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp));
    }

    @Test
    void confirmRegistration_inputOtpDiffersFromStoredOtp_throws() {
        val email = randomStringWithNonZeroLength();
        val otp1 = randomStringWithNonZeroLength();
        val otp2 = differentOtp(otp1);
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        when(otpService.read(anyString()))
                .thenReturn(otp2);

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp1));
    }

    @Test
    void confirmRegistration_userNotFound_throws() {
        val email = randomStringWithNonZeroLength();
        val otp = randomStringWithNonZeroLength();
        val exceptionMessage = "User email or password are incorrect.";
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        when(otpService.read(anyString()))
                .thenReturn(otp);
        doThrow(new UsernameNotFoundException(exceptionMessage))
                .when(keepingUserService)
                .read(anyString());

        assertThrows(UsernameNotFoundException.class, () -> registrationService.confirmRegistration(email, otp));
    }

    @Test
    void confirmRegistration_ok() {
        val name = randomStringWithNonZeroLength();
        val age = nextInt(User.MIN_AGE, User.MAX_AGE + 1);
        val email = "a@a.com";
        val userJson = String.format("{"
                        + "\"id\":null,"
                        + "\"name\":\"%s\","
                        + "\"age\":%d,"
                        + "\"email\":\"%s\","
                        + "\"profile\":null,"
                        + "\"phones\":[]"
                        + "}",
                name, age, email);
        val user = new User(name, age, email);
        val otp = randomStringWithNonZeroLength();
        val registrationService = new RegistrationServiceImpl(cashService, hazelcastFactory, mailService);

        when(cashService.save(user))
                .thenReturn(user);
        when(otpService.read(anyString()))
                .thenReturn(otp);
        when(keepingUserService.read(anyString()))
                .thenReturn(userJson);
        registrationService.confirmRegistration(email, otp);

        verify(otpService, times(1)).read(anyString());
        verify(keepingUserService, times(1)).read(anyString());
        verify(cashService, times(1)).save(any());
    }

    private String differentOtp(final String otp1) {
        return Stream.generate(TestUtils::randomStringWithNonZeroLength)
                .limit(10)
                .filter(otp2 -> !otp1.equals(otp2))
                .findFirst()
                .orElseThrow();
    }

}