package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistence.db.model.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface ResourceService {


    List<User> getUsers(Specification<User> specification, Pageable pageable);

    int updateUser(JsonNode patch, long id);

    void updatePhones(Map<String, String> changes, long userId);

    void deletePhones(long userId);
}
