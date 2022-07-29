package com.example.hhvolgograd.persistance.grid.service;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import lombok.val;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Objects.requireNonNull;

public class HazelcastManagerTestFactory {

    public HazelcastManager factory() {
        val properties = properties();
        val clientConfig = clientConfig(properties);
        val instance = HazelcastClient.newHazelcastClient(clientConfig);

        return new HazelcastManager(instance);
    }

    private Properties properties() {
        try (
                InputStream input = KeepingUserServiceImplTest
                        .class
                        .getClassLoader()
                        .getResourceAsStream("hazelcast-client.yaml")
        ) {
            val yamlProcessor = new YamlPropertiesFactoryBean();

            yamlProcessor.setResources(new InputStreamResource(requireNonNull(input)));
            return yamlProcessor.getObject();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ClientNetworkConfig clientNetworkingConfig(Properties properties) {
        val clientNetworkingConfig = new ClientNetworkConfig();
        val clusterMember = properties.getProperty("hazelcast-client.network.cluster-members[0]");
        val redoOperation = (boolean) properties.get("hazelcast-client.network.redo-operation");

        clientNetworkingConfig.addAddress(clusterMember);
        clientNetworkingConfig.setRedoOperation(redoOperation);
        return clientNetworkingConfig;
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
}
