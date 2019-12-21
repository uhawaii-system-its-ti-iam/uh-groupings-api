package edu.hawaii.its.api.access;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class User extends org.springframework.security.core.userdetails.User {

    public static final long serialVersionUID = 2L;
    private String uhUuid;
    private UhAttributes attributes;

    // Constructor.
    public User(String username, String uhUuid, Collection<GrantedAuthority> authorities) {
        super(username, "", authorities);
        setUhUuid(uhUuid);
    }

    // Constructor.
    public User(String username, Collection<GrantedAuthority> authorities) {
        this(username, null, authorities);
    }

    public String getUid() {
        return getUsername();
    }

    public String getUhUuid() {
        return uhUuid;
    }

    public void setUhUuid(String uhUuid) {
        this.uhUuid = uhUuid;
    }

    public String getAttribute(String name) {
        return attributes.getValue(name);
    }

    public UhAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(UhAttributes attributes) {
        this.attributes = attributes;
    }

    public String getName() {
        return attributes.getValue("cn");
    }

    public boolean isRole(Role role) {
        return getAuthorities().contains(new SimpleGrantedAuthority(role.longName()));
    }

    @Override
    public String toString() {
        return "User [uid=" + getUid()
                + ", uhUuid=" + getUhUuid()
                + ", super-class: " + super.toString() + "]";
    }
}
