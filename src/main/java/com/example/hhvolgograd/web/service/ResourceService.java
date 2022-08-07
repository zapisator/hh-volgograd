package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ResourceService {


    List<User> getUsers(Specification<User> specification, Pageable pageable);

    User update();
}
