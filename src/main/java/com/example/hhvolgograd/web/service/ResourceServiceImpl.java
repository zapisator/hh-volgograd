package com.example.hhvolgograd.web.service;

import com.example.hhvolgograd.persistance.db.model.User;
import com.example.hhvolgograd.persistance.db.service.CashService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ResourceServiceImpl implements ResourceService {

    private final CashService service;

    @Override
    public Page<User> getUsers(Specification<User> specification, Pageable pageable) {
        return service.findAll(specification, pageable);
    }
}