package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.core.DistributedObject;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

@Testcontainers
class KeepingUserServiceImplTest {

    @Container
    private static final DockerComposeContainer<?> hazelcastMemberContainer;
    private static final HazelcastManager factory;

    private KeepingUserService service;

    static {
        hazelcastMemberContainer = new HazelcastMemberContainerFactory().hazelcastMemberContainer();
        hazelcastMemberContainer.start();
        factory = new HazelcastManagerTestFactory().factory();
    }

    @BeforeEach
    public void setUp() {
        service = new KeepingUserServiceImpl(factory);
    }

    @AfterEach
    public void tearDown() {
        factory
                .getInstance()
                .getDistributedObjects()
                .forEach(DistributedObject::destroy);
    }

    @ParameterizedTest
    @NullSource
    void save_nullEmail_throws(String email) {
        assertThrows(
                NullPointerException.class,
                () -> service.save(email, randomAlphanumeric(20))
        );
    }

    @ParameterizedTest
    @NullSource
    void save_nullUserJson_throws(String userJson) {
        assertThrows(
                NullPointerException.class,
                () -> service.save(randomAlphanumeric(20), userJson)
        );
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void save_EmptyOrWhitespaceAsEmail_ok(String email) {
        val userJson = randomAlphanumeric(20);

        assertDoesNotThrow(() -> service.save(email, userJson));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void save_EmptyOrWhitespaceAsUserJson_ok(String userJson) {
        val email = randomAlphanumeric(20);

        assertDoesNotThrow(() -> service.save(email, userJson));
    }

    @Test
    void save_afterHaveAlreadyBeenSavedAndNotYetExpired_throws()
            throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        val email = randomAlphanumeric(20);
        val userJson = randomAlphanumeric(20);
        val secondsLessThanStorageTime = new Random().nextInt(KeepingUserServiceImpl.storageTime);
        val sleepPeriod = Duration.ofSeconds(secondsLessThanStorageTime).toMillis();

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.SECONDS, KeepingUserServiceImpl.class);
        service.save(email, userJson);
        sleep(sleepPeriod);

        assertThrows(TooEarlyToContactServiceException.class, () -> service.save(email, userJson));
    }

    @Test
    void save_afterExpired_ok() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        val email = randomAlphanumeric(20);
        val userJson = randomAlphanumeric(20);

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.NANOSECONDS, KeepingUserServiceImpl.class);
        service.save(email, userJson);
        sleep(1);

        assertDoesNotThrow(() -> service.save(email, userJson));
    }

    @Test
    void save_firstTime_ok() {
        val email = randomAlphanumeric(20);
        val userJson = randomAlphanumeric(20);

        assertDoesNotThrow(() -> service.save(email, userJson));
    }

    @ParameterizedTest
    @NullSource
    void findUserByEmail_throwsOnNull(String email) {
        assertThrows(NullPointerException.class, () -> service.findUserByEmail(email));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void findUserByEmail_onEmptyOrWhitespace_ok(String email) {
        assertDoesNotThrow(() -> service.findUserByEmail(email));
    }

    @RepeatedTest(100)
    void findUserByEmail_onAnySingleEmail_ok() {
        val email = RandomStringUtils.random(new Random().nextInt(100));

        assertDoesNotThrow(() -> service.findUserByEmail(email));
    }
}