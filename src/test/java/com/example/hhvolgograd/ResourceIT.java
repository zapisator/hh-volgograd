package com.example.hhvolgograd;

import com.example.hhvolgograd.persistance.db.model.Phone;
import com.example.hhvolgograd.persistance.db.model.Profile;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import com.example.hhvolgograd.persistance.db.service.DbCashService;
import com.example.hhvolgograd.web.rest.ResourceController;
import com.example.hhvolgograd.web.service.ResourceServiceImpl;
import com.turkraft.springfilter.boot.SpecificationFilterArgumentResolver;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@ContextConfiguration(initializers = {ResourceIT.DockerPostgresDataSourceInitializer.class})
//@Import({NativeSqlUserRepositoryImpl.class, QueryBuilderImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
public class ResourceIT {

    @Container
    @SuppressWarnings({"resource"})
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(RepositoryIT.class)))
            .withDatabaseName("postgres")
            .withUsername("hhVolgograd")
            .withPassword("passwordForHhVolgograd");

    @Autowired
    private UserRepository userRepository;
    private MockMvc mockMvc;

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

    @BeforeEach
    public void setUp() {
        val cashService = new DbCashService(userRepository);
        val resourceService = new ResourceServiceImpl(cashService);
        val resourceController = new ResourceController(resourceService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void twentyConsecutivelyNumberedUsers_justFilter_correct() throws Exception {
        val path = "/resource/users";
        val requiredId = 6;
        val ageGreaterThan = 2;
        val ageLessThan = 5;
        val query = "?"
                + "filter= id:" + requiredId + " or (age > " + ageGreaterThan + " and age < " + ageLessThan + ")";

        fillTheDb();

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasItem(new LambdaMatcher<>(expressionResult -> {
                            val map = (Map<String, Object>) expressionResult;
                            val id = (int)map.get("id");
                            val age = (int)map.get("age");

                            return id == requiredId || (age > ageGreaterThan && age < ageLessThan );
                        }, "id == " + requiredId
                                + " or (age > " + ageGreaterThan + " && age < " + ageLessThan + ")")))
                );
    }

    @Test
    public void twentyConsecutivelyNumberedUsers_pageSize19_gives19users() throws Exception {
        val path = "/resource/users";
        val pageSize = 19;
        val query = "?"
                + "size=" + pageSize;

        fillTheDb();

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasSize(pageSize))
                );
    }

    @RepeatedTest(10)
    public void twentyConsecutivelyNumberedUsers_unexpectedQuery_givesDefaultNumberOfUsers() throws Exception {
        val path = "/resource/users";
        val query = "?" + randomStringWithNonZeroLength();
        val defaultNumberOfUsers = 3;

        fillTheDb();

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasSize(defaultNumberOfUsers))
                );
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
