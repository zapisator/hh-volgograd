package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.web.service.LoginService;
import com.example.hhvolgograd.web.service.RegistrationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(
        name = "Authentication and authorization",
        description = "Registration and login with their confirmation methods"
)
public class AuthController {

    public static final String SIGN_IN_ON_ANSWER =
            "We sent the code to the address you provided when you %1$s."
                    + "%nPlease complete your %1$s by texting the address and the code to us."
                    + "%nIf you do not see the message in your inbox, please check spam.";

    private RegistrationService registrationService;
    private LoginService loginService;

    @PostMapping(value = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "A method getting all initial information about a user to register with email confirmation")
    public ResponseEntity<String> register(@Valid @RequestBody User user) throws JsonProcessingException {
        registrationService.register(user);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/auth/registration-confirmation")
                .body(format(SIGN_IN_ON_ANSWER, "registration"));
    }

    @GetMapping(value = "/registration-confirmation")
    @Operation(summary = "A method getting One Time Password confirmation for previously registered user")
    public ResponseEntity<String> confirm(
            @Valid @RequestParam String email,
            @RequestParam String otp) {
        registrationService.confirmRegistration(email, otp);
        return ResponseEntity.ok(format("User with email '%s' is successfully registered.", email));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "A method getting email of a registered user to send email request back to confirm login")
    public ResponseEntity<String> login(@RequestParam String email) {
        loginService.login(email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, "/auth/token")
                .body(format(SIGN_IN_ON_ANSWER, "login"));
    }

    @GetMapping("/token")
    @Operation(summary = "A method getting email and One Time Password. Its response is an authorization token")
    public ResponseEntity<String> token(@RequestParam String email, @RequestParam String otp) {
        val token = loginService.token(email, otp);

        return ResponseEntity.ok(token);
    }

}
