package com.example.hhvolgograd;

import com.example.hhvolgograd.persistance.db.model.Phone;
import com.example.hhvolgograd.persistance.db.model.Profile;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.hhvolgograd.persistance.model.User.MAX_AGE;
import static com.example.hhvolgograd.persistance.model.User.MIN_AGE;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ContextConfiguration(initializers = {RepositoryIT.DockerPostgresDataSourceInitializer.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Slf4j
public class RepositoryIT {

    private static final String VALID_EMAIL = "a@gmail.com";
    private static final String VALID_NAME = "name";
    public static final int VALID_AGE = nextInt(MIN_AGE, MAX_AGE + 1);
    private static final Class<ConstraintViolationException> EXCEPTION_CLASS = ConstraintViolationException.class;

    @Container
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("postgres")
            .withUsername("hhVolgograd")
            .withPassword("passwordForHhVolgograd");

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

    @Autowired
    private UserRepository userRepository;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void test_userName_mayBeBlankOrNull(String name) {
        val initialUser = new User(name, VALID_AGE, VALID_EMAIL);

        val returnedUser = userRepository.save(new User(name, VALID_AGE, VALID_EMAIL));

        assertAll(
                () -> assertEquals(initialUser.getName(), returnedUser.getName()),
                () -> assertEquals(initialUser.getAge(), returnedUser.getAge()),
                () -> assertEquals(initialUser.getEmail(), returnedUser.getEmail()),
                () -> assertNotNull(returnedUser.getId())
        );
    }

    @ParameterizedTest
    @MethodSource("alphanumericName")
    public void test_userName_isAnyAlphanumeric(String name) {
        val initialUser = new User(name, VALID_AGE, VALID_EMAIL);

        val returnedUser = userRepository.save(initialUser);

        assertAll(
                () -> assertEquals(initialUser.getName(), returnedUser.getName()),
                () -> assertEquals(initialUser.getAge(), returnedUser.getAge()),
                () -> assertEquals(initialUser.getEmail(), returnedUser.getEmail()),
                () -> assertNotNull(returnedUser.getId())
        );
    }

    @ParameterizedTest
    @MethodSource("negativeOrExcessiveAge")
    public void test_userAge_throwsOnNegativeOrExcessive(int age) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, age, VALID_EMAIL))
        );
    }

    @ParameterizedTest
    @MethodSource("agesInRange")
    public void test_userAge_isInRange(int age) {
        val initialUser = new User(VALID_NAME, age, VALID_EMAIL);

        val returnedUser = userRepository.save(initialUser);

        assertAll(
                () -> assertEquals(initialUser.getName(), returnedUser.getName()),
                () -> assertEquals(initialUser.getAge(), returnedUser.getAge()),
                () -> assertEquals(initialUser.getEmail(), returnedUser.getEmail()),
                () -> assertNotNull(returnedUser.getId())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void test_userEmail_throwsOnBlank(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailImproperCombinations")
    public void test_userEmail_throwsOnImproperCombinations(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailValidValues")
    public void test_userEmail_hasProperCombinations(String email) {
        val initialUser = new User(email, VALID_AGE, VALID_EMAIL);

        val returnedUser = userRepository.save(initialUser);

        assertAll(
                () -> assertEquals(initialUser.getName(), returnedUser.getName()),
                () -> assertEquals(initialUser.getAge(), returnedUser.getAge()),
                () -> assertEquals(initialUser.getEmail(), returnedUser.getEmail()),
                () -> assertNotNull(returnedUser.getId())
        );
    }

    @Test
    public void test_userEmail_throwsOnDuplicate() {
        val anotherValidName = VALID_NAME + "2";
        val theSameEmail = VALID_EMAIL;

        userRepository.save(new User(VALID_NAME, VALID_AGE, theSameEmail));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.save(new User(anotherValidName, VALID_AGE, theSameEmail))
        );
    }

    @ParameterizedTest
    @MethodSource("positiveCash")
    public void test_profileCash_mayBeAnyPositiveValue(double cash) {
        val profileInitial = new Profile(new BigDecimal(cash));
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithProfile(profileInitial);

        val userReturned = userRepository.save(userInitial);
        val profileReturned = userReturned.getProfile();

        assertAll(
                () -> assertEquals(userInitial.getName(), userReturned.getName()),
                () -> assertEquals(userInitial.getAge(), userReturned.getAge()),
                () -> assertEquals(userInitial.getEmail(), userReturned.getEmail()),
                () -> assertNotNull(userReturned.getId()),
                () -> assertEquals(profileInitial.getCash(), profileReturned.getCash()),
                () -> assertEquals(profileReturned.getUser().getId(), userReturned.getId()),
                () -> assertNotNull(profileReturned.getId())
        );
    }

    @ParameterizedTest
    @MethodSource("negativeCash")
    public void test_profileCash_throwsOnNegativeValues(double cash) {
        val profileInitial = new Profile(new BigDecimal(cash));
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithProfile(profileInitial);

        assertThrows(EXCEPTION_CLASS, () -> userRepository.save(userInitial));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void test_phoneValue_mayBeNullOrEmpty(String value) {
        val phoneInitial = new Phone(value);
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithPhone(phoneInitial);

        val userReturned = userRepository.save(userInitial);
        val phoneReturned = Arrays.stream(
                        userReturned.getPhones().toArray(new Phone[0])
                )
                .collect(Collectors.toList())
                .get(0);

        assertAll(
                () -> assertEquals(userInitial.getName(), userReturned.getName()),
                () -> assertEquals(userInitial.getAge(), userReturned.getAge()),
                () -> assertEquals(userInitial.getEmail(), userReturned.getEmail()),
                () -> assertNotNull(userReturned.getId()),
                () -> assertEquals(phoneInitial, phoneInitial),
                () -> assertNotNull(phoneReturned.getId())
        );
    }

    @ParameterizedTest
    @MethodSource("alphanumericPhoneValue")
    public void test_phoneValue_isAnyAlphanumeric(String value) {
        val phoneInitial = new Phone(value);
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithPhone(phoneInitial);

        val userReturned = userRepository.save(userInitial);
        val phoneReturned = Arrays.stream(
                        userReturned.getPhones().toArray(new Phone[0])
                )
                .collect(Collectors.toList())
                .get(0);

        assertAll(
                () -> assertEquals(userInitial.getName(), userReturned.getName()),
                () -> assertEquals(userInitial.getAge(), userReturned.getAge()),
                () -> assertEquals(userInitial.getEmail(), userReturned.getEmail()),
                () -> assertNotNull(userReturned.getId()),
                () -> assertEquals(phoneInitial, phoneInitial),
                () -> assertNotNull(phoneReturned.getId())
        );
    }

    private static Stream<Arguments> alphanumericName() {
        return Stream.generate(() -> randomAlphanumeric(nextInt(1, 20)))
                .limit(10)
                .map(Arguments::of);
    }

    private static Stream<Arguments> negativeOrExcessiveAge() {
        val negativeAges = Stream.generate(() -> nextInt(1, Integer.MAX_VALUE) * -1)
                .limit(10)
                .map(Arguments::of);
        val positiveForbiddenAges = IntStream.range(0, MIN_AGE)
                .mapToObj(number -> Arguments.of(nextInt(0, MIN_AGE)));
        val excessiveAges = Stream.generate(() -> nextInt(MAX_AGE, Integer.MAX_VALUE))
                .limit(10)
                .map(Arguments::of);

        return Stream.concat(
                Stream.concat(negativeAges, excessiveAges),
                positiveForbiddenAges
        );
    }

    public static Stream<Arguments> agesInRange() {
        return Stream.generate(() -> nextInt(MIN_AGE, MAX_AGE + 1))
                .limit(10)
                .map(Arguments::of);
    }

    public static Stream<Arguments> emailImproperCombinations() {
        return Stream.of(
                "plainaddress",
                "#@%^%#$@#$@#.com",
                "@example.com",
                "Joe Smith <email@example.com>",
                "email.example.com",
                "email@example@example.com",
                ".email@example.com",
                "email.@example.com",
                "email..email@example.com",
                "あいうえお@example.com",
                "email@example.com (Joe Smith)",
                "email@example",
                "email@-example.com",
                "email@example.web",
                "email@111.222.333.44444",
                "email@example..com",
                "Abc..123@example.com",
                "List of Strange Invalid Email Addresses",
                "”(),:;<>[\\]@example.com",
                "just”not”right@example.com",
                "this\\ is\"really\"not\\allowed@example.com"
        ).map(Arguments::of);
    }

    public static Stream<Arguments> emailValidValues() {
        return Stream.of(
                "email@example.com",
                "firstname.lastname@example.com",
                "email@subdomain.example.com",
                "firstname+lastname@example.com",
                "1234567890@example.com",
                "email@example-one.com",
                "_______@example.com",
                "email@example.name",
                "email@example.museum",
                "email@example.co.jp",
                "firstname-lastname@example.com"
        ).map(Arguments::of);
    }

    public static Stream<Arguments> positiveCash() {
        return Stream.generate(() -> nextDouble(0D, 1_000_000D))
                .limit(10)
                .map(Arguments::of);
    }

    public static Stream<Arguments> negativeCash() {
        val max = 0D + 0.01;

        return Stream.generate(() -> nextDouble(max, 1_000_000D) * -1)
                .limit(10)
                .map(Arguments::of);
    }

    public static Stream<Arguments> alphanumericPhoneValue() {
        return Stream.generate(() -> randomAlphanumeric(nextInt(1, 20)))
                .limit(10)
                .distinct()
                .map(Arguments::of);
    }

}
