package com.example.hhvolgograd;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.example.hhvolgograd.persistance.grid.service.HazelcastMapServiceImpl;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;
import java.util.UUID;

import static com.example.hhvolgograd.TestUtils.randomStringWithNonZeroLength;
import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HazelcastMapServiceIT {

    @Container
    private static DockerComposeContainer<?> hazelcastMemberContainer;
    private static HazelcastInstance instance;

    @BeforeAll
    public static void firstSetUp() {
        val clientConfig = new HazelcastClientProperty().clientConfig();

        hazelcastMemberContainer = new HazelcastMemberContainerFactory().hazelcastMemberContainer();
        hazelcastMemberContainer.start();
        instance = HazelcastClient.newHazelcastClient(clientConfig);
    }

    @AfterAll
    public static void lastTearDown() {
        instance.shutdown();
        hazelcastMemberContainer.stop();
    }

    @AfterEach
    public void tearDown() {
        instance
                .getDistributedObjects()
                .forEach(DistributedObject::destroy);
    }

    @ParameterizedTest
    @NullSource
    void save_nullEmail_throws(String email) {
        val otp = UUID.randomUUID().toString();
        val service = new HazelcastMapServiceImpl(instance.getMap(randomStringWithNonZeroLength()), Duration.ofSeconds(1));

        assertThrows(NullPointerException.class, () -> service.save(email, otp));
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {" ", "  "})
    void save_EmptyOrWhitespaceAsEmail_ok(String email) {
        val otp = UUID.randomUUID().toString();
        val storageTime = Duration.ofMillis(10);
        val service = new HazelcastMapServiceImpl(instance.getMap(randomStringWithNonZeroLength()), storageTime);

        assertDoesNotThrow(() -> service.save(email, otp));
    }

    @Test
    void save_afterHaveAlreadyBeenSavedAndNotYetExpired_throws() throws InterruptedException {
        val email = randomStringWithNonZeroLength();
        val otp = UUID.randomUUID().toString();
        val storageTime = Duration.ofSeconds(2);
        val sleepPeriod = storageTime.minusSeconds(1).toMillis();
        val mapName = randomStringWithNonZeroLength();
        final IMap<String, String> map = instance.getMap(mapName);
        val service = new HazelcastMapServiceImpl(map, storageTime);

        service.save(email, otp);
        sleep(sleepPeriod);

        assertThrows(TooEarlyToContactServiceException.class, () -> service.save(email, otp));
    }

    @Test
    void save_afterExpired_ok() throws InterruptedException {
        val email = randomStringWithNonZeroLength();
        val otp = UUID.randomUUID().toString();
        val storageTime = Duration.ofMillis(10);
        val sleepPeriod = storageTime.plusMillis(2).toMillis();
        val mapName = randomStringWithNonZeroLength();
        final IMap<String, String> map = instance.getMap(mapName);
        val service = new HazelcastMapServiceImpl(map, storageTime);

        service.save(email, otp);
        sleep(sleepPeriod);

        assertDoesNotThrow(() -> service.save(email, otp));
    }

    @Test
    void save_firstTime_ok() {
        val email = randomStringWithNonZeroLength();
        val otp = UUID.randomUUID().toString();
        val storageTime = Duration.ofMillis(10);
        val service = new HazelcastMapServiceImpl(instance.getMap(randomStringWithNonZeroLength()), storageTime);

        assertDoesNotThrow(() -> service.save(email, otp));
    }

    @ParameterizedTest
    @NullSource
    void getOtpOrThrow_throwsOnNullEmail(String email) {
        val storageTime = Duration.ofMillis(10);
        val service = new HazelcastMapServiceImpl(instance.getMap(randomStringWithNonZeroLength()), storageTime);

        assertThrows(NullPointerException.class, () -> service.read(email));
    }

}