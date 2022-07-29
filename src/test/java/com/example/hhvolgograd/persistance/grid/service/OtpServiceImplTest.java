//package com.example.hhvolgograd.persistance.grid.service;
//
//import com.example.hhvolgograd.exception.TooEarlyToContactServiceException;
//import com.hazelcast.client.HazelcastClient;
//import com.hazelcast.client.config.ClientConfig;
//import com.hazelcast.client.config.ClientNetworkConfig;
//import com.hazelcast.core.DistributedObject;
//import lombok.val;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.RepeatedTest;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.EmptySource;
//import org.junit.jupiter.params.provider.NullSource;
//import org.junit.jupiter.params.provider.ValueSource;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
//import org.springframework.core.io.InputStreamResource;
//import org.testcontainers.containers.GenericContainer;
//import org.testcontainers.containers.output.Slf4jLogConsumer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//import java.time.Duration;
//import java.util.Properties;
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//
//import static java.lang.Thread.sleep;
//import static java.util.Objects.requireNonNull;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
//
//class OtpServiceImplTest {
//
//    private HazelcastManager factory;
//    private KeepingUserService service;
//
//    @Container
//    public static GenericContainer<?> hazelcastMemberContainer = new GenericContainer<>("hazelcast/hazelcast:latest")
//            .withExposedPorts(5701)
//            .withEnv("HZ_NETWORK_PUBLICADDRESS", "localhost:5701")
//            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(KeepingUserServiceImplTest.class)))
//            .withAccessToHost(true);
//
//
//    @BeforeEach
//    public void setUp() {
//        val properties = properties();
//        val clientConfig = clientConfig(properties);
//        val instance = HazelcastClient.newHazelcastClient(clientConfig);
//
//        if (!hazelcastMemberContainer.isRunning()) {
//            hazelcastMemberContainer.start();
//        }
//        factory = new HazelcastManager(instance);
//        service = new KeepingUserServiceImpl(factory);
//    }
//
//    @AfterEach
//    public void tearDown() {
//        factory
//                .getInstance()
//                .getDistributedObjects()
//                .forEach(DistributedObject::destroy);
//        factory
//                .getInstance()
//                .shutdown();
//        factory = null;
//    }
//
//    @ParameterizedTest
//    @NullSource
//    void save_nullEmail_throws(String email) {
//        assertThrows(
//                NullPointerException.class,
//                () -> service.save(email, randomAlphanumeric(20))
//        );
//    }
//
//    @ParameterizedTest
//    @NullSource
//    void save_nullUserJson_throws(String userJson) {
//        assertThrows(
//                NullPointerException.class,
//                () -> service.save(randomAlphanumeric(20), userJson)
//        );
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = {" ", "  "})
//    void save_EmptyOrWhitespaceAsEmail_ok(String email) {
//        val userJson = randomAlphanumeric(20);
//
//        assertDoesNotThrow(() -> service.save(email, userJson));
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = {" ", "  "})
//    void save_EmptyOrWhitespaceAsUserJson_ok(String userJson) {
//        val email = randomAlphanumeric(20);
//
//        assertDoesNotThrow(() -> service.save(email, userJson));
//    }
//
//    @Test
//    void save_afterHaveAlreadyBeenSavedAndNotYetExpired_throws() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
//        val email = randomAlphanumeric(20);
//        val userJson = randomAlphanumeric(20);
//        val secondsLessThanStorageTime = new Random().nextInt(KeepingUserServiceImpl.storageTime);
//        val sleepPeriod = Duration.ofSeconds(secondsLessThanStorageTime).toMillis();
//
//        onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.SECONDS);
//        service.save(email, userJson);
//        sleep(sleepPeriod);
//
//        assertThrows(TooEarlyToContactServiceException.class, () -> service.save(email, userJson));
//    }
//
//    @Test
//    void save_afterExpired_ok() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
//        val email = randomAlphanumeric(20);
//        val userJson = randomAlphanumeric(20);
//
//        onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit.NANOSECONDS);
//        service.save(email, userJson);
//        sleep(1);
//
//        assertDoesNotThrow(() -> service.save(email, userJson));
//    }
//
//    @Test
//    void save_firstTime_ok() {
//        val email = randomAlphanumeric(20);
//        val userJson = randomAlphanumeric(20);
//
//        assertDoesNotThrow(() -> service.save(email, userJson));
//    }
//
//    @ParameterizedTest
//    @NullSource
//    void findOtpByEmail_throwsOnNull(String email) {
//        assertThrows(NullPointerException.class, () -> service.findUserByEmail(email));
//    }
//
//    @ParameterizedTest
//    @EmptySource
//    @ValueSource(strings = {" ", "  "})
//    void findOtpByEmail_onEmptyOrWhitespace_ok(String email) {
//        assertDoesNotThrow(() -> service.findUserByEmail(email));
//    }
//
//    @RepeatedTest(100)
//    void findOtpByEmail_onAnySingleEmail_ok() {
//        val email = RandomStringUtils.random(new Random().nextInt(100));
//
//        assertDoesNotThrow(() -> service.findUserByEmail(email));
//    }
//
//    private static void onceToSpeedUpTest_ChangeWithReflectionFinalTimeUnit(TimeUnit timeUnit)
//            throws NoSuchFieldException, IllegalAccessException {
//        Field field = OtpServiceImpl.class.getDeclaredField("unit");
//        field.setAccessible(true);
//
//        Field modifiersField = Field.class.getDeclaredField("modifiers");
//        modifiersField.setAccessible(true);
//        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
//        field.set(null, timeUnit);
//    }
//
//    private ClientConfig clientConfig(Properties properties) {
//        val clientConfig = new ClientConfig();
//        val clusterName = properties.getProperty("hazelcast-client.cluster-name");
//        val instanceName = properties.getProperty("hazelcast-client.instance-name");
//        val clientNetworkingConfig = clientNetworkingConfig(properties);
//
//        clientConfig.setClusterName(clusterName);
//        clientConfig.setInstanceName(instanceName);
//        clientConfig.setNetworkConfig(clientNetworkingConfig);
//        return clientConfig;
//    }
//
//    private ClientNetworkConfig clientNetworkingConfig(Properties properties) {
//        val clientNetworkingConfig = new ClientNetworkConfig();
//        val clusterMember = properties.getProperty("hazelcast-client.network.cluster-members[0]");
//        val redoOperation = (boolean) properties.get("hazelcast-client.network.redo-operation");
//
//        clientNetworkingConfig.addAddress(clusterMember);
//        clientNetworkingConfig.setRedoOperation(redoOperation);
//        return clientNetworkingConfig;
//    }
//
//    private Properties properties() {
//        try (
//                InputStream input = this.getClass().getClassLoader().getResourceAsStream("hazelcast-client.yaml")
//        ) {
//            val yamlProcessor = new YamlPropertiesFactoryBean();
//
//            yamlProcessor.setResources(new InputStreamResource(requireNonNull(input)));
//            return yamlProcessor.getObject();
//        } catch (IOException ex) {
//            throw new RuntimeException(ex);
//        }
//    }
//}