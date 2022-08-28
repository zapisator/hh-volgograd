package com.example.hhvolgograd;

import com.example.hhvolgograd.persistence.db.model.Phone;
import com.example.hhvolgograd.persistence.db.model.Profile;
import com.example.hhvolgograd.persistence.db.model.User;
import com.example.hhvolgograd.persistence.db.repository.PhoneRepository;
import com.example.hhvolgograd.persistence.db.repository.UserRepository;
import com.example.hhvolgograd.persistence.db.service.DbCashService;
import com.example.hhvolgograd.web.rest.ResourceController;
import com.example.hhvolgograd.web.service.ResourceServiceImpl;
import com.turkraft.springfilter.boot.SpecificationFilterArgumentResolver;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DataJpaTest
@ContextConfiguration(initializers = {ResourceIT.DockerPostgresDataSourceInitializer.class})
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
    @Autowired
    private PhoneRepository phoneRepository;
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
        val cashService = new DbCashService(userRepository, phoneRepository);
        val resourceService = new ResourceServiceImpl(cashService);
        val resourceController = new ResourceController(resourceService);

        fillTheDb();

        mockMvc = MockMvcBuilders
                .standaloneSetup(resourceController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new SpecificationFilterArgumentResolver()
                )
                .build();
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    public void deletePhones_userWithoutPhones_deleteAllPhonesOfTheUser_noContent() throws Exception {
        val user = userRepository
                .findAll()
                .stream()
                .findFirst()
                .orElseThrow();
        val userId = user.getId();
        val path = "/resource/user/" + userId + "/deleting/phones";

        phoneRepository.deleteAllByUserId(user.getId());

        mockMvc
                .perform(
                        delete(URI.create(path))
                )
                .andExpect(status().isNoContent());
    }

    @Test
    public void updatePhones_tenConsecutivelyNumberedUsersWithPhoneEach_deleteAllPhoneOfTheUser_ok() throws Exception {
        val users = userRepository
                .findAll()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        val user = users.get(nextInt(0, users.size()));
        val userId = user.getId();
        val userWithPhones = IntStream.range(0, 5)
                .mapToObj(i -> new Phone(Long.toString(8_903_800_0100L + i * 100L)))
                .map(user::userWithPhone)
                .findFirst()
                .orElseThrow();
        val path = "/resource/user/" + userId + "/deleting/phones";

        userRepository.save(userWithPhones);

        mockMvc
                .perform(
                        delete(URI.create(path))
                )
                .andExpect(status().isOk());
    }

    @Test
    public void updatePhones_tenConsecutivelyNumberedUsersWithPhoneEach_updatePhoneWithNull_ok() throws Exception {
        val users = userRepository
                .findAll()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        val phone = new Phone(Long.toString(8_903_000_00_00L));
        val user = users.get(nextInt(0, users.size()))
                .userWithPhone(phone);
        val userId = user.getId();
        val path = "/resource/user/" + userId + "/updating/phones";

        userRepository.save(user);

        mockMvc
                .perform(
                        patch(URI.create(path))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param(phone.getValue(), (String) null)
                )
                .andExpect(status().isOk());
        System.out.println(Arrays.toString(userRepository.findUserByEmail(user.getEmail()).orElseThrow().getPhones().toArray()));
    }

    @Test
    public void updatePhones_twentyConsecutivelyNumberedUsersWithPhoneEach_updatePhoneWithNewValue_ok() throws Exception {
        val users = userRepository
                .findAll()
                .stream()
                .limit(10)
                .collect(Collectors.toList());
        val phone = new Phone(Long.toString(8_903_000_00_00L));
        val newPhoneValue = Long.toString(Long.parseLong(phone.getValue()) + 10L);
        val user = users.get(nextInt(0, users.size()))
                .userWithPhone(phone);
        val userId = user.getId();
        val path = "/resource/user/" + userId + "/updating/phones";

        userRepository.save(user);

        mockMvc
                .perform(
                        patch(URI.create(path))
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param(phone.getValue(), newPhoneValue)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void updateUser_twentyConsecutivelyNumberedUsers_updateNameAgeWithNull_statusOk() throws Exception {
        val users = userRepository.findAll();
        val id = users.get(nextInt(0, users.size())).getId();
        val path = "/resource/user/" + id + "/updating";
        val patchString = patchString(
                List.of(
                        new String[]{"\"replace\"", "\"/name\"", null},
                        new String[]{"\"replace\"", "\"/age\"", null}
                )
        );

        mockMvc
                .perform(
                        patch(URI.create(path))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(patchString)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void updateUser_twentyConsecutivelyNumberedUsers_updateNameAgeEmail_statusOk() throws Exception {
        val users = userRepository.findAll();
        val id = users.get(nextInt(0, users.size())).getId();
        val path = "/resource/user/" + id + "/updating";
        val patchString = patchString(
                List.of(
                        new String[]{"\"replace\"", "\"/name\"", "\"eman\""},
                        new String[]{"\"replace\"", "\"/age\"", "2"},
                        new String[]{"\"replace\"", "\"/email\"", "\"b@marrou.com\""}
                )
        );

        mockMvc
                .perform(
                        patch(URI.create(path))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(patchString)
                )
                .andExpect(status().isOk());
    }

    @Test
    public void updateUser_twentyConsecutivelyNumberedUsers_updateNameAgeEmail_WithWrongUserId_statusNoContent() throws Exception {
        val id = userRepository
                .findAll()
                .stream()
                .map(User::getId)
                .map(Long::intValue)
                .max(Integer::compareTo)
                .orElseThrow()
                + 1;
        val path = "/resource/user/" + id + "/updating";
        val patchString = patchString(
                List.of(
                        new String[]{"\"replace\"", "\"/name\"", "\"eman\""},
                        new String[]{"\"replace\"", "\"/age\"", "2"},
                        new String[]{"\"replace\"", "\"/email\"", "\"b@marrou.com\""}
                )
        );

        mockMvc
                .perform(
                        patch(URI.create(path))
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(patchString)
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void list_twentyConsecutivelyNumberedUsers_justFilter_correct() throws Exception {
        val path = "/resource/users";
        val requiredId = 6;
        val ageGreaterThan = 2;
        val ageLessThan = 5;
        val query = "?"
                + "filter= id:" + requiredId + " or (age > " + ageGreaterThan + " and age < " + ageLessThan + ")";

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasItem(new LambdaMatcher<>(expressionResult -> {
                            val map = (Map<String, Object>) expressionResult;
                            val id = (int) map.get("id");
                            val age = (int) map.get("age");

                            return id == requiredId || (age > ageGreaterThan && age < ageLessThan);
                        }, "id == " + requiredId
                                + " or (age > " + ageGreaterThan + " && age < " + ageLessThan + ")")))
                );
    }

    @Test
    public void list_twentyConsecutivelyNumberedUsers_pageSize19_gives19users() throws Exception {
        val path = "/resource/users";
        val pageSize = 19;
        val query = "?"
                + "size=" + pageSize;

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasSize(pageSize))
                );
    }

    @RepeatedTest(10)
    public void list_twentyConsecutivelyNumberedUsers_unexpectedQuery_givesDefaultNumberOfUsers() throws Exception {
        val path = "/resource/users";
        val query = "?" + randomStringWithNonZeroLength();
        val defaultNumberOfUsers = 3;

        mockMvc
                .perform(get(path + query))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*]", hasSize(defaultNumberOfUsers))
                );
    }

    private void fillTheDb() {
        val count = 20;
        val users = Stream.iterate(0, n -> n + 1)
                .limit(count)
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

    private String patchString(List<String[]> members) {
        val operationIndex = 0;
        val pathIndex = 1;
        val valueIndex = 2;

        return members
                .stream()
                .map(member -> member[operationIndex].equals("remove")
                        ? removeMember(member[operationIndex], member[pathIndex])
                        : addAndReplaceMember(member[operationIndex], member[pathIndex], member[valueIndex])
                )
                .collect(Collectors.joining(", \n", "[\n", "\n]"));
    }

    private String addAndReplaceMember(String operation, String path, String value) {
        return "\t{ \"op\": " + operation + ", \"path\":" + path + ", \"value\":" + value + " }";
    }

    private String removeMember(String operation, String path) {
        return "{ \"op\": " + operation + ", \"path\":" + path + " }";
    }
}
