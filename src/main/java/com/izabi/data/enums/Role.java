package com.izabi.data.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER,
    ADMIN,
    STUDENT;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

}
