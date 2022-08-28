package com.example.hhvolgograd.web.rest;

import com.example.hhvolgograd.web.service.ResourceService;
import com.turkraft.springfilter.boot.SpecificationFilterArgumentResolver;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceControllerTest {
// test exceptions and ok cases especially when controller advice will be in the game
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
}