package com.example.hhvolgograd.persistance.grid.service;

public interface HazelcastMapService {
    void save(String email, String value);
    String read(String email);
}
