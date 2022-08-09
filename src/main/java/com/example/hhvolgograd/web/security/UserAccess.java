package com.example.hhvolgograd.web.security;

import lombok.val;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("unused")
public class UserAccess {

    public boolean checkId(Authentication authentication, int id) {
        val scopePrefix = "SCOPE_";

        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(scopePrefix + id));
    }
}
