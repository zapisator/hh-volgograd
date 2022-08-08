package com.example.hhvolgograd;

import lombok.val;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static java.io.File.separator;

public class ContainerFactory {

    public DockerComposeContainer hazelcastMemberContainer() {
        val containerExposedPort = 5701;
        val serviceName = "hazelcast-member";

        return dockerComposeContainer(serviceName, containerExposedPort);
    }

    public DockerComposeContainer dockerComposeContainer(String serviceName, int containerExposedPort) {
        val path = System.getProperty("user.dir") + separator + "containers" + separator + "docker-compose.yaml";
        val file = new File(path);

        return new DockerComposeContainer(file)
                .withExposedService(serviceName, containerExposedPort, Wait.forListeningPort())
                .withLocalCompose(true);
    }
}
