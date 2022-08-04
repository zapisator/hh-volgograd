package com.example.hhvolgograd.web.security;

import lombok.Getter;

@Getter
public enum Scope {

    USERINFO_READ ("userinfo.read"),
    USERINFO_WRITE ("userinfo.write"),
    PROFILE_READ ("profile.read"),
    PHONES_READ ("phones.read"),
    PHONES_WRITE ("phones.write");

    final String value;

    Scope(String value) {
        this.value = value;
    }
}
