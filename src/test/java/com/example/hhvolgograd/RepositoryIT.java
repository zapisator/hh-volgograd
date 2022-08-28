package com.example.hhvolgograd;

import com.example.hhvolgograd.persistance.db.model.Phone;
import com.example.hhvolgograd.persistance.db.model.Profile;
import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.model.dto.Entry;
import com.example.hhvolgograd.persistance.db.model.dto.UserUpdates;
import com.example.hhvolgograd.persistance.db.repository.PhoneRepository;
import com.example.hhvolgograd.persistance.db.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.example.hhvolgograd.persistance.db.model.User.MAX_AGE;
import static com.example.hhvolgograd.persistance.db.model.User.MIN_AGE;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomUtils.nextDouble;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    @SuppressWarnings({"resource"})
    public static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(RepositoryIT.class)))
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
    @Autowired
    private PhoneRepository phoneRepository;

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

    @RepeatedTest(10)
    public void deletePhonesByUserIdAndValues_deleteOneOfPhonesOfExistingUser_ok() {
        val phonesSize = 10;
        val phones = phonesCreatedInAmountOf();
        val savedUser = userWithPhonesSavedAtDataBase(phones);
        val phoneToDeleteIndex = nextInt(0, phonesSize);
        val phoneValueToDelete = new LinkedList<>(savedUser.getPhones()).get(phoneToDeleteIndex);
        val phonesToDelete = List.of(new Entry<>("value", phoneValueToDelete.getValue()));

        val deletedCount = phoneRepository.deletePhonesByUserIdAndValues(savedUser.getId(), phonesToDelete);

        assertEquals(1, deletedCount);
    }

    @Test
    public void deletePhonesByUserIdAndValues_deletePhoneWithIncorrectValue_throws() {
        val phones = phonesCreatedInAmountOf();
        val savedUser = userWithPhonesSavedAtDataBase(phones);
        val phonesToDelete = List.of(new Entry<>("value", "\n\n\n\t"));

        assertThrows(
                ValidationException.class,
                () -> phoneRepository.deletePhonesByUserIdAndValues(savedUser.getId(), phonesToDelete)
        );
    }

    @Test
    public void deletePhonesByUserIdAndValues_deletePhonesExistingUserDoesNotHave_ok() {
        val phonesSize = 10;
        val phones = phonesCreatedInAmountOf();
        val savedUser = userWithPhonesSavedAtDataBase(phones);
        val phoneToDeleteIndex = nextInt(0, phonesSize);
        val phoneValueToDelete = new LinkedList<>(savedUser.getPhones()).get(phoneToDeleteIndex);
        val phonesToDelete = List.of(new Entry<>("value", phoneValueToDelete.getValue() + "10"));

        val deletedCount = phoneRepository.deletePhonesByUserIdAndValues(savedUser.getId(), phonesToDelete);

        assertEquals(0, deletedCount);
    }

    @Test
    public void addPhonesByUserIdAndValues_addOnePhoneToUserPhones_ok() {
        val phones = phonesCreatedInAmountOf();
        val savedUser = userWithPhonesSavedAtDataBase(phones);
        val phonesToAdd = List.of(new Entry<>("value", "89048000100"));

        val addedCount = phoneRepository.addPhonesByUserId(savedUser.getId(), phonesToAdd);

        assertEquals(1, addedCount);
    }

    @RepeatedTest(10)
    public void addPhonesByUserIdAndValues_addOneIncorrectPhoneToUserPhones_throws() {
        val phones = phonesCreatedInAmountOf();
        val savedUser = userWithPhonesSavedAtDataBase(phones);
        val phonesToAdd = List.of(new Entry<>("value", "\n\n\n"));

        assertThrows(
                ValidationException.class,
                () -> phoneRepository.addPhonesByUserId(savedUser.getId(), phonesToAdd)
        );
    }

    @ParameterizedTest
    @MethodSource("alphanumericName")
    public void updateUser_alreadySavedUser_updateName_ok(String name) {
        val initialUser = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val returnedUser = userRepository.save(initialUser);

        val userUpdates = UserUpdates.create(Map.of("name", name));
        val id = returnedUser.getId();
        userRepository.update(userUpdates, id);
        val userName = userRepository.findById(id).orElseThrow().getName();

        assertEquals(name, userName);
    }

    @ParameterizedTest
    @MethodSource("agesInRange")
    public void updateUser_alreadySavedUser_updateAge_ok(int age) {
        val initialUser = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val returnedUser = userRepository.save(initialUser);
        val userUpdates = UserUpdates.create(Map.of("age", Integer.toString(age)));
        val id = returnedUser.getId();

        userRepository.update(userUpdates, id);
        val userAge = userRepository.findById(id).orElseThrow().getAge();

        assertEquals(age, userAge);
    }

    @ParameterizedTest
    @MethodSource("negativeOrExcessiveAge")
    public void updateUser_updateWithInvalidAge_throws(int age) {
        val initialUser = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val returnedUser = userRepository.save(initialUser);

        val userUpdates = UserUpdates.create(Map.of("age", Integer.toString(age)));
        val id = returnedUser.getId();

        assertThrows(ValidationException.class, () -> userRepository.update(userUpdates, id));
    }

    @ParameterizedTest
    @MethodSource("emailValidValues")
    public void updateUser_updateEmailWithoutIntersections_ok(String email) {
        val initialUser = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val returnedUser = userRepository.save(initialUser);
        val userUpdates = UserUpdates.create(Map.of("email", email));
        val id = returnedUser.getId();

        userRepository.update(userUpdates, id);
        val userEmail = userRepository.findById(id).orElseThrow().getEmail();

        assertEquals(email, userEmail);
    }

    @ParameterizedTest
    @MethodSource("emailValidValues")
    public void updateUser_updateEmailWithIntersections_throws(String email) {
        val initialUser1 = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val initialUser2 = new User(VALID_NAME + 1, VALID_AGE, email);
        userRepository.save(initialUser1);
        val returnedUser2 = userRepository.save(initialUser2);
        val userUpdates = UserUpdates.create(Map.of("email", VALID_EMAIL));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRepository.update(userUpdates, returnedUser2.getId().intValue())
        );
    }

    @ParameterizedTest
    @MethodSource("emailImproperCombinations")
    public void updateUser_updateEmailWithIncorrectValue_throws(String email) {
        val initialUser1 = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val initialUser2 = new User(VALID_NAME + 1, VALID_AGE, VALID_EMAIL.replace("gmail", "rambler"));
        userRepository.save(initialUser1);
        val returnedUser2 = userRepository.save(initialUser2);
        val userUpdates = UserUpdates.create(Map.of("email", email));

        assertThrows(
                ValidationException.class,
                () -> userRepository.update(userUpdates, returnedUser2.getId().intValue())
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void save_userName_mayBeBlankOrNull(String name) {
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
    public void saveUser_userName_isAnyAlphanumeric(String name) {
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
    public void save_userAge_throwsOnNegativeOrExcessive(int age) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, age, VALID_EMAIL))
        );
    }

    @ParameterizedTest
    @MethodSource("agesInRange")
    public void save_userAge_isInRange(int age) {
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
    public void save_userEmail_throwsOnBlank(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailImproperCombinations")
    public void save_userEmail_throwsOnImproperCombinations(String email) {
        assertThrows(EXCEPTION_CLASS, () ->
                userRepository.save(new User(VALID_NAME, VALID_AGE, email))
        );
    }

    @ParameterizedTest
    @MethodSource("emailValidValues")
    public void save_userEmail_hasProperCombinations(String email) {
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
    public void save_userEmail_throwsOnDuplicate() {
        val anotherValidName = VALID_NAME + "2";
        val theSameEmail = VALID_EMAIL;

        userRepository.save(new User(VALID_NAME, VALID_AGE, theSameEmail));

        assertThrows(DataIntegrityViolationException.class, () ->
                userRepository.save(new User(anotherValidName, VALID_AGE, theSameEmail))
        );
    }

    @ParameterizedTest
    @MethodSource("positiveCash")
    public void save_profileCash_mayBeAnyPositiveValue(double cash) {
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
    public void save_profileCash_throwsOnNegativeValues(double cash) {
        val profileInitial = new Profile(new BigDecimal(cash));
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithProfile(profileInitial);

        assertThrows(EXCEPTION_CLASS, () -> userRepository.save(userInitial));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    public void save_phoneValueIsNullOrEmpty_throws(String value) {
        val phoneInitial = new Phone(value);
        val userInitial = new User(VALID_NAME, VALID_AGE, VALID_EMAIL)
                .userWithPhone(phoneInitial);

        assertThrows(ConstraintViolationException.class, () -> userRepository.save(userInitial));
    }

    @ParameterizedTest
    @MethodSource("alphanumericPhoneValue")
    public void save_phoneValue_isAnyAlphanumeric(String value) {
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

    @NotNull
    private User userWithPhonesSavedAtDataBase(List<Phone> phones) {
        val user = new User(VALID_NAME, VALID_AGE, VALID_EMAIL);
        val savedUser = userRepository.save(user);
        savedUser.setPhones(phones);
        userRepository.save(savedUser);

        return savedUser;
    }


    @NotNull
    private List<Phone> phonesCreatedInAmountOf() {
        return IntStream
                .range(0, 10)
                .mapToObj(i -> {
                    val phone = new Phone();

                    phone.setValue(Long.valueOf(8_903_800_0100L + i).toString());
                    return phone;
                })
                .collect(Collectors.toList());
    }
}
