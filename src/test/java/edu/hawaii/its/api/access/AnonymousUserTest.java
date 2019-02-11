package edu.hawaii.its.api.access;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
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
        assertEquals("anonymous", user.getUsername());
        assertEquals("anonymous", user.getUid());
        assertEquals(null, user.getUhuuid());
        assertEquals("", user.getPassword());
        assertEquals(1, user.getAuthorities().size());
        assertTrue(user.isRole(Role.ANONYMOUS));
    }
}
