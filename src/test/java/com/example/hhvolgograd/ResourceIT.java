package com.example.hhvolgograd;

import com.example.hhvolgograd.persistance.db.model.Phone;
import com.example.hhvolgograd.persistance.db.model.Profile;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import com.example.hhvolgograd.persistance.db.service.DbCashService;
import com.example.hhvolgograd.web.rest.ResourceController;
import com.example.hhvolgograd.web.service.ResourceService;
import com.example.hhvolgograd.web.service.ResourceServiceImpl;
import com.turkraft.springfilter.boot.SpecificationFilterArgumentResolver;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@ContextConfiguration(initializers = {ResourceIT.DockerPostgresDataSourceInitializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ResourceIT {

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(RepositoryIT.class)))
            .withDatabaseName("postgres")
            .withUsername("hhVolgograd")
            .withPassword("passwordForHhVolgograd");

    @Autowired
    private UserRepository userRepository;
    private ResourceService resourceService;

    static class DockerPostgresDataSourceInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {


        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }

    }

    @PostConstruct
    public void set() {
        val cashService = new DbCashService(userRepository);
        resourceService = new ResourceServiceImpl(cashService);
    }

    @Test
    public void twentyConsecutivelyNumberedUsers_justFilter_correct() throws Exception {
        val path = "/resource/users";
        val requiredId = 6;
        val ageGreaterThan = 2;
        val ageLessThan = 5;
        val query = "?"
                + "filter= id:" + requiredId + " or (age > " + ageGreaterThan + " and age < " + ageLessThan + ")";
        val resourceController = new ResourceController(resourceService);
        val mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();

        fillTheDb();
        val resultActions = mockMvc
                .perform(get(path + query));

        val result = resultActions
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        val users = toUsers(result);
        assertAll(
                () -> assertNotNull(users),
                () -> assertEquals(3, users.size()),
                () -> assertThat(users)
                        .allMatch(user
                                -> user.getId() == requiredId
                                || (user.getAge() > ageGreaterThan && user.getAge() < ageLessThan)
                        )
        );
    }

    @Test
    public void twentyConsecutivelyNumberedUsers_pageSize19_gives19users() throws Exception {
        val path = "/resource/users";
        val pageSize = 19;
        val query = "?"
                + "size=" + pageSize;
        val resourceController = new ResourceController(resourceService);
        val mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();

        fillTheDb();
        val resultActions = mockMvc
                .perform(get(path + query));

        val result = resultActions
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        val users = toUsers(result);
        assertAll(
                () -> assertNotNull(users),
                () -> assertEquals(19, users.size())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "?snthSNTHI=eeeeeeeeeee..",
            "?fiter= sntahsntoseu"
    })
    public void twentyConsecutivelyNumberedUsers_unexpectedQuery_givesDefaultNumberOfUsers(String query) throws Exception {
        val path = "/resource/users";
        val defaultNumberOfUsers = 3;
        val resourceController = new ResourceController(resourceService);
        val mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();

        fillTheDb();
        val resultActions = mockMvc
                .perform(get(path + query));

        val result = resultActions
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        val users = toUsers(result);
        assertAll(
                () -> assertNotNull(users),
                () -> assertEquals(defaultNumberOfUsers, users.size())
        );
    }

    @NotNull
    private static List<User> toUsers(String result) throws IOException {
        return List.of(new ObjectMapper().readValue(result, User[].class));
    }

    private void fillTheDb() {
        val users = Stream.iterate(0, n -> n + 1)
                .limit(20)
                .map(this::testUser)
                .collect(Collectors.toList());

        userRepository.saveAll(users);
    }

    private User testUser(int integer) {
        val user = new User("name" + integer, integer, "a" + integer + "@mail.ru");
        val phone = new Phone(String.valueOf(8_900_000_00_00L + integer));
        val profile = new Profile(BigDecimal.valueOf(integer));

        return user
                .userWithPhone(phone)
                .userWithProfile(profile);
    }
}
