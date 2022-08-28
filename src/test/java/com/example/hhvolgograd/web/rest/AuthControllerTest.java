package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.configuration.JwtProperty;
import com.example.hhvolgograd.web.security.token.TokenParameter;
import com.example.hhvolgograd.web.security.token.TokenService;
import com.example.hhvolgograd.web.service.resource.ResourceService;
import com.turkraft.springfilter.boot.SpecificationFilterArgumentResolver;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    @Test
    void register() {
        val jwtProperty = new JwtProperty();
        jwtProperty.setSecret("hhVolgogradSecretMustHave32chars+");

        val token = new TokenService(jwtProperty).generate(
                TokenParameter
                        .create()
                        .withEmail("a@mail.ru")
                        .withUserId("1")
                        .withScope("1")
                        .build()
        );
        System.out.println(token);
    }

    @Test
    void list() throws Exception {
        val resourceService = mock(ResourceService.class);
        val resourceController = new ResourceController(resourceService);
        val query = "?filter= id:5 or (age > 20 and age < 50)";
        val mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();

        mockMvc
                .perform(get("/resource/users" + query))
                .andExpect(status().isOk());
    }

    @Test
    void login() {
        val tokenSerialized = "eyJhbGciOiJIUzI1NiJ9"
                + ".eyJzdWIiOiJhQGdtYWlsLmNvbSIsInNjb3BlIjoiYUBnbWFpbC5jb20iLCJpc3MiOiJoaFZvbGdvZ3J"
                + "hZCIsImV4cCI6MTk3Mjc2Mjg4NCwiaWF0IjoxNjU3NDAyODg0LCJlbWFpbCI6ImFAZ21haWwuY29tIn0"
                + ".dX7sfWuwDtEzvNMxMQt33xmG57Yc-0CriV-tNv8pwTY";

    }

}