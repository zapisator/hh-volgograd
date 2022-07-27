package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.web.service.RegistrationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    public static final String REGISTRATION_ANSWER =
            "We sent the code to the address you provided when you registered."
            + "\nPlease complete your registration by texting the address and the code to us."
            + "\nIf you do not see the message in your inbox, please check spam.";

    private RegistrationService registrationService;

    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> register(@Valid @RequestBody User user) {
        registrationService.register(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/auth/registration-confirmation")
                .body(REGISTRATION_ANSWER);
    }


}
