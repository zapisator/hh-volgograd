package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ResourceService {


    Page<User> getUsers(Specification<User> specification, Pageable pageable);
}
