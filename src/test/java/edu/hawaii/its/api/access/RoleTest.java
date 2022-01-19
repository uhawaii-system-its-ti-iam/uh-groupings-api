package edu.hawaii.its.api.access;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class RoleTest {

    @Test
    public void longName() {
        for (Role role : Role.values()) {
            assertThat(role.longName(), is("ROLE_" + role.name()));
        }
    }

    @Test
    public void find() {
        Role role = Role.find(Role.ADMIN.name());
        assertNotNull(role);
        assertThat(role.name(), equalTo(Role.ADMIN.name()));
        assertThat(role.longName(), equalTo(Role.ADMIN.longName()));
        assertThat(role.toString(), equalTo("ROLE_ADMIN"));
        role = Role.find("non-existent-role");
        assertNull(role);
    }
}
