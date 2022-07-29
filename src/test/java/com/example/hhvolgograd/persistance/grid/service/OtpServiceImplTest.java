package com.example.hhvolgograd.persistance.grid.service;

import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
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
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.InputStreamResource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.io.File.separator;
import static java.lang.Thread.sleep;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

class OtpServiceImplTest {

    private HazelcastManager factory;
    private OtpService service;

    @Container
    public static DockerComposeContainer<?> hazelcastMemberContainer;

    static {
        val containerExposedPort = 5701;
        val path = System.getProperty("user.dir") + separator + "containers" + separator + "docker-compose.yaml";
        val file = new File(path);
        val serviceName = "hazelcast-member";

        hazelcastMemberContainer = new DockerComposeContainer<>(file)
                .withExposedService(serviceName, containerExposedPort, Wait.forListeningPort());
        hazelcastMemberContainer.start();
    }

    @BeforeEach
    public void setUp() {
        val properties = properties();
        val clientConfig = clientConfig(properties);
        val instance = HazelcastClient.newHazelcastClient(clientConfig);

        factory = new HazelcastManager(instance);
        service = new OtpServiceImpl(factory);
    }

    @AfterEach
    public void tearDown() {
        factory
                .getInstance()
                .getDistributedObjects()
                .forEach(DistributedObject::destroy);
        factory
                .getInstance()
                .shutdown();
        factory = null;
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

        onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.SECONDS);
        service.save(email);
        sleep(sleepPeriod);

        assertThrows(TooEarlyToContactServiceException.class, () -> service.save(email));
    }

    @Test
    void save_afterExpired_ok() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        val email = randomAlphanumeric(20);

        onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.NANOSECONDS);
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

    private static void onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit timeUnit)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = OtpServiceImpl.class.getDeclaredField("unit");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, timeUnit);
    }

    private ClientConfig clientConfig(Properties properties) {
        val clientConfig = new ClientConfig();
        val clusterName = properties.getProperty("hazelcast-client.cluster-name");
        val instanceName = properties.getProperty("hazelcast-client.instance-name");
        val clientNetworkingConfig = clientNetworkingConfig(properties);

        clientConfig.setClusterName(clusterName);
        clientConfig.setInstanceName(instanceName);
        clientConfig.setNetworkConfig(clientNetworkingConfig);
        return clientConfig;
    }

    private ClientNetworkConfig clientNetworkingConfig(Properties properties) {
        val clientNetworkingConfig = new ClientNetworkConfig();
        val clusterMember = properties.getProperty("hazelcast-client.network.cluster-members[0]");
        val redoOperation = (boolean) properties.get("hazelcast-client.network.redo-operation");

        clientNetworkingConfig.addAddress(clusterMember);
        clientNetworkingConfig.setRedoOperation(redoOperation);
        return clientNetworkingConfig;
    }

    private Properties properties() {
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("hazelcast-client.yaml")) {
            val yamlProcessor = new YamlPropertiesFactoryBean();

            yamlProcessor.setResources(new InputStreamResource(requireNonNull(input)));
            return yamlProcessor.getObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}