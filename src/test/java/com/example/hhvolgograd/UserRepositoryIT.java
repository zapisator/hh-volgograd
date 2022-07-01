package com.example.hhvolgograd;

import com.example.hhvolgograd.persistance.entity.User;
import com.example.hhvolgograd.persistance.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.validation.ConstraintViolationException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.hhvolgograd.persistance.entity.User.MAX_AGE;
import static com.example.hhvolgograd.persistance.entity.User.MIN_AGE;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Slf4j
public class UserRepositoryIT extends RepositoryIT {

    private static final String VALID_EMAIL = "a@gmail.com";
    private static final String VALID_NAME = "name";
    public static final int VALID_AGE = nextInt(MIN_AGE, MAX_AGE + 1);
    private static final Class<ConstraintViolationException> EXCEPTION_CLASS = ConstraintViolationException.class;

    @Autowired
    private UserRepository userRepository;

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void test_name_mayBeBlankOrNull(String name) {
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
    @MethodSource("alphanumericLength")
    public void test_name_isAnyAlphanumeric(String name) {
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
    public void test_age_throwsOnNegativeOrExcessive(int age) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, age, VALID_EMAIL))
        );
    }

    @ParameterizedTest
    @MethodSource("agesInRange")
    public void test_age_isInRange(int age) {
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
    public void test_email_throwsOnBlank(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailImproperCombinations")
    public void test_email_throwsOnImproperCombinations(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailValidValues")
    public void test_email_hasProperCombinations(String email) {
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
    public void test_email_throwsOnDuplicate() {
        val anotherValidName = VALID_NAME + "2";
        val theSameEmail = VALID_EMAIL;

        userRepository.save(new User(VALID_NAME, VALID_AGE, theSameEmail));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.save(new User(anotherValidName, VALID_AGE, theSameEmail))
        );
    }

    private static Stream<Arguments> alphanumericLength() {
        return Stream.generate(() -> RandomStringUtils.randomAlphanumeric(nextInt(1, 20)))
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

}
