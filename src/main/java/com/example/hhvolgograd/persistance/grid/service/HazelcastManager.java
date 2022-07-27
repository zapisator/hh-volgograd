package com.example.hhvolgograd.persistance.grid.service;

import com.hazelcast.core.HazelcastInstance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
@AllArgsConstructor
public class HazelcastManager {

    private final HazelcastInstance instance;

}
