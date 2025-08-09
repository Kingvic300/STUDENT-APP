package com.izabi.data.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER,
    ADMIN,
    MODERATOR;

    @Override
    public String getAuthority() {
        return "ROLE_" + this.name();
    }

}
