package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.core.DistributedObject;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OtpServiceImplTest {

    @Container
    private static final DockerComposeContainer<?> hazelcastMemberContainer;
    private static final HazelcastManager factory;

    private OtpService otpService;

    static {
        hazelcastMemberContainer = new HazelcastMemberContainerFactory().hazelcastMemberContainer();
        hazelcastMemberContainer.start();
        factory = new HazelcastManagerTestFactory().factory();
    }

    @BeforeEach
    public void setUp() {
        otpService = new OtpServiceImpl(factory);
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
                () -> otpService.save(email)
        );
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void save_EmptyOrWhitespaceAsEmail_ok(String email) {
        assertDoesNotThrow(() -> otpService.save(email));
    }

    @Test
    void save_afterHaveAlreadyBeenSavedAndNotYetExpired_throws()
            throws NoSuchFieldException, IllegalAccessException, InterruptedException
    {
        val email = randomStringWithNonZeroLength();
        val secondsLessThanStorageTime = new Random().nextInt(KeepingUserServiceImpl.storageTime);
        val sleepPeriod = Duration.ofSeconds(secondsLessThanStorageTime).toMillis();

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.SECONDS, OtpServiceImpl.class);
        otpService.save(email);
        sleep(sleepPeriod);

        assertThrows(TooEarlyToContactServiceException.class, () -> otpService.save(email));
    }

    @Test
    void save_afterExpired_ok() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        val email = randomStringWithNonZeroLength();

        new HazelcastClientUnitChanger()
                .onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.NANOSECONDS, OtpServiceImpl.class);
        otpService.save(email);
        sleep(1);

        assertDoesNotThrow(() -> otpService.save(email));
    }

    @Test
    void save_firstTime_ok() {
        val email = randomStringWithNonZeroLength();

        assertDoesNotThrow(() -> otpService.save(email));
    }

    @ParameterizedTest
    @NullSource
    void getOtpOrThrow_throwsOnNullEmail(String email) {
        assertThrows(NullPointerException.class, () -> otpService.getOtpOrThrow(email));
    }

}