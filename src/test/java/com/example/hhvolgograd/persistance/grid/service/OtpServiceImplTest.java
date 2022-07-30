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
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

class OtpServiceImplTest {

    @Container
    private static final DockerComposeContainer<?> hazelcastMemberContainer;
    private static final HazelcastManager factory;

    private OtpService service;

    static {
        hazelcastMemberContainer = new HazelcastMemberContainerFactory().hazelcastMemberContainer();
        hazelcastMemberContainer.start();
        factory = new HazelcastManagerTestFactory().factory();
    }

    @BeforeEach
    public void setUp() {
        service = new OtpServiceImpl(factory);
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
                () -> service.save(email)
        );
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void save_EmptyOrWhitespaceAsEmail_ok(String email) {
        assertDoesNotThrow(() -> service.save(email));
    }

    @Test
    void save_afterHaveAlreadyBeenSavedAndNotYetExpired_throws()
            throws NoSuchFieldException, IllegalAccessException, InterruptedException
    {
        val email = randomAlphanumeric(20);
        val secondsLessThanStorageTime = new Random().nextInt(KeepingUserServiceImpl.storageTime);
        val sleepPeriod = Duration.ofSeconds(secondsLessThanStorageTime).toMillis();

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.SECONDS, OtpServiceImpl.class);
        service.save(email);
        sleep(sleepPeriod);

        assertThrows(TooEarlyToContactServiceException.class, () -> service.save(email));
    }

    @Test
    void save_afterExpired_ok() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        val email = randomAlphanumeric(20);

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.NANOSECONDS, OtpServiceImpl.class);
        service.save(email);
        sleep(1);

        assertDoesNotThrow(() -> service.save(email));
    }

    @Test
    void save_firstTime_ok() {
        val email = randomAlphanumeric(20);

        assertDoesNotThrow(() -> service.save(email));
    }

    @ParameterizedTest
    @NullSource
    void findOtpByEmail_throwsOnNull(String email) {
        assertThrows(NullPointerException.class, () -> service.findOtpByEmail(email));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void findOtpByEmail_onEmptyOrWhitespace_ok(String email) {
        assertDoesNotThrow(() -> service.findOtpByEmail(email));
    }

    @RepeatedTest(100)
    void findOtpByEmail_onAnySingleEmail_ok() {
        val email = RandomStringUtils.random(new Random().nextInt(100));

        assertDoesNotThrow(() -> service.findOtpByEmail(email));
    }
}