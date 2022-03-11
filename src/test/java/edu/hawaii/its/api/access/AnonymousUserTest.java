package edu.hawaii.its.api.access;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AnonymousUserTest {

    private User user;

    @BeforeEach
    public void setUp() {
        user = new AnonymousUser();
    }

    @Test
    public void testConstructions() {
        assertNotNull(user);
        assertThat(user.getUsername(), is("anonymous"));
        assertThat(user.getUid(), is("anonymous"));
        assertThat(user.getUhUuid(), equalTo((Class<Object>) null));
        assertThat(user.getPassword(), is(""));
        assertThat(user.getAuthorities().size(), is(1));
        assertTrue(user.isRole(Role.ANONYMOUS));
    }
}
