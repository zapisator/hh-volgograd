package com.example.hhvolgograd.persistence.grid.service;

public interface HazelcastMapService {
    void save(String email, String value);
    String read(String email);
}
