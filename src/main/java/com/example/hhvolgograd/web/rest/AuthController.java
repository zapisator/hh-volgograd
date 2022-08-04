package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.web.service.LoginService;
import com.example.hhvolgograd.web.service.RegistrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static java.lang.String.format;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {

    public static final String SIGN_IN_ON_ANSWER =
            "We sent the code to the address you provided when you %1$s."
                    + "%nPlease complete your %1$s by texting the address and the code to us."
                    + "%nIf you do not see the message in your inbox, please check spam.";

    private RegistrationService registrationService;
    private LoginService loginService;

    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> register(@Valid @RequestBody User user) throws JsonProcessingException {
        registrationService.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/auth/registration-confirmation")
                .body(format(SIGN_IN_ON_ANSWER, "registration"));
    }

    @GetMapping(value = "/registration-confirmation")
    public ResponseEntity<String> confirm(@Valid @RequestParam String email, @RequestParam String otp) {
        registrationService.confirmRegistration(email, otp);
        return ResponseEntity.ok(format("User with email '%s' is successfully registered.", email));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> login(@RequestParam String email) {
        loginService.login(email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/auth/token")
                .body(format(SIGN_IN_ON_ANSWER, "login"));
    }

    @GetMapping("/token")
    public ResponseEntity<String> token(@RequestParam String email, @RequestParam String otp) {
        val token = loginService.token(email, otp);

        return ResponseEntity.ok(token);
    }


}
