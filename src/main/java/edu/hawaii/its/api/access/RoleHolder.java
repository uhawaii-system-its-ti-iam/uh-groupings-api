package edu.hawaii.its.api.access;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedHashSet;
import java.util.Set;

public class RoleHolder {

    private Set<GrantedAuthority> authorities = new LinkedHashSet<>();

    public void add(Role role) {
        authorities.add(new SimpleGrantedAuthority(role.longName()));
    }

    public Set<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public int size() {
        return authorities.size();
    }

    @Override
    public String toString() {
        return "RoleHolder [authorities=" + getAuthorities() + "]";
    }
}
