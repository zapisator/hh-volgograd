package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.mail.service.MailService;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import com.example.hhvolgograd.persistance.grid.service.KeepingUserServiceImpl;
import com.example.hhvolgograd.persistance.grid.service.OtpServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RegistrationServiceImplTest {

    private final Random random = new Random();

    @Test
    void register_alreadyRegisteredEmail_throws() throws JsonProcessingException {
        val user = mock(User.class);
        val keepingUserService = mock(KeepingUserServiceImpl.class);
        val otpService = mock(OtpServiceImpl.class);
        val mailService = mock(MailService.class);
        val cashService = mock(CashService.class);
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(user.getEmail()).thenReturn(RandomStringUtils.random(1 + random.nextInt(19)));
        when(user.toJson()).thenReturn(RandomStringUtils.random(30 + random.nextInt(30)));
        doNothing().when(keepingUserService).save(anyString(), anyString());
        when(otpService.save(anyString())).thenReturn(RandomStringUtils.random(32));
        doNothing().when(mailService).send(anyString(), anyString());
        when(cashService.existsUserByEmail(anyString())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> registrationService.register(user));
    }

    @Test
    void register_notRegisteredYetEmail_ok() throws JsonProcessingException {
        val user = mock(User.class);
        val keepingUserService = mock(KeepingUserServiceImpl.class);
        val otpService = mock(OtpServiceImpl.class);
        val mailService = mock(MailService.class);
        val cashService = mock(CashService.class);
        val registrationService = new RegistrationServiceImpl(cashService, otpService, keepingUserService, mailService);

        when(user.getEmail()).thenReturn(RandomStringUtils.random(1 + random.nextInt(19)));
        when(user.toJson()).thenReturn(RandomStringUtils.random(30 + random.nextInt(30)));
        doNothing().when(keepingUserService).save(anyString(), anyString());
        when(otpService.save(anyString())).thenReturn(RandomStringUtils.random(32));
        doNothing().when(mailService).send(anyString(), anyString());
        when(cashService.existsUserByEmail(anyString())).thenReturn(false);

        assertDoesNotThrow(() -> registrationService.register(user));
    }

    @Test
    void confirmRegistration() {
    }

}