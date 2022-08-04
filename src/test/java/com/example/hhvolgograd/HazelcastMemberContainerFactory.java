package com.example.hhvolgograd;

import lombok.val;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static java.io.File.separator;

public class HazelcastMemberContainerFactory {

    public DockerComposeContainer hazelcastMemberContainer() {
        val containerExposedPort = 5701;
        val path = System.getProperty("user.dir") + separator + "containers" + separator + "docker-compose.yaml";
        val file = new File(path);
        val serviceName = "hazelcast-member";

        return new DockerComposeContainer<>(file)
                .withExposedService(serviceName, containerExposedPort, Wait.forListeningPort());
    }
}
