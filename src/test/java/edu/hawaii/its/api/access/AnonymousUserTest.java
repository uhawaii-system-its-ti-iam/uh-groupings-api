package edu.hawaii.its.api.access;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AnonymousUserTest {

    private User user;

    @Before
    public void setUp() {
        user = new AnonymousUser();
    }

    @Test
    public void testConstructions() {
        assertNotNull(user);
        assertThat(user.getUsername(), is("anonymous"));
        assertThat(user.getUid(), is("anonymous"));
        assertThat(user.getUhUuid(), is((Class<Object>) null));
        assertThat(user.getPassword(), is(""));
        assertThat(user.getAuthorities().size(), is(1));
        assertTrue(user.isRole(Role.ANONYMOUS));
    }
}
