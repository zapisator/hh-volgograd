package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ResourceService {


    List<User> getUsers(Specification<User> specification, Pageable pageable);

    void updateUser(JsonNode patch, long id);

    void updatePhones(JsonNode patch, long userId);

    void deletePhones(long userId);
}
