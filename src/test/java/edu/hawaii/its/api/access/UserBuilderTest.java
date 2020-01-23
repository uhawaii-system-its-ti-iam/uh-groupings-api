package edu.hawaii.its.api.access;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserBuilderTest {

    @Autowired
    private UserBuilder userBuilder;

    @Ignore
    @Test
    public void testAdminUsers() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhUuid", "89999999");
        User user = userBuilder.make(map);

        // Basics.
        assertThat(user.getUsername(), is("duckart"));
        assertThat(user.getUid(), is("duckart"));
        assertThat(user.getUhUuid(), is("89999999"));

        // Granted Authorities.
        assertTrue(user.getAuthorities().size() > 0);
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));
        assertTrue(user.isRole(Role.ADMIN));

        map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhUuid", "10000001");
        user = userBuilder.make(map);

        assertThat(user.getUsername(), is("someuser"));
        assertThat(user.getUid(), is("someuser"));
        assertThat(user.getUhUuid(), is("10000001"));

        assertTrue(user.getAuthorities().size() > 0);
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));
        assertTrue(user.isRole(Role.ADMIN));
    }

    @Ignore
    @Test
    public void testEmployees() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "jjcale");
        map.put("uhUuid", "10000004");
        User user = userBuilder.make(map);

        // Basics.
        assertThat(user.getUsername(), is("jjcale"));
        assertThat(user.getUid(), is("jjcale"));
        assertThat(user.getUhUuid(), is("10000004"));

        // Granted Authorities.
        assertThat(user.getAuthorities().size(), is(3));
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));

        assertFalse(user.isRole(Role.ADMIN));
    }

    @Ignore
    @Test
    public void testEmployeesWithMultivalueUid() {
        Map<String, Object> map = new HashMap<>();
        ArrayList<Object> uids = new ArrayList<>();
        uids.add("aaaaaaa");
        uids.add("bbbbbbb");
        map.put("uid", uids);
        map.put("uhUuid", "10000003");
        User user = userBuilder.make(map);

        // Basics.
        assertThat(user.getUsername(), is("aaaaaaa"));
        assertThat(user.getUid(), is("aaaaaaa"));
        assertThat(user.getUhUuid(), is("10000003"));

        // Granted Authorities.
        assertThat(user.getAuthorities().size(), is(4));
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));
        assertTrue(user.isRole(Role.ADMIN));
    }

    @Ignore
    @Test
    public void testNotAnEmployee() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "nobody");
        map.put("uhUuid", "10000009");
        User user = userBuilder.make(map);

        // Basics.
        assertThat(user.getUsername(), is("nobody"));
        assertThat(user.getUid(), is("nobody"));
        assertThat(user.getUhUuid(), is("10000009"));

        // Granted Authorities.
        assertThat(user.getAuthorities().size(), is(2));
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertFalse(user.isRole(Role.EMPLOYEE));
        assertFalse(user.isRole(Role.ADMIN));
    }

    @Ignore
    @Test
    public void testUidNull() {
        List<String> uids = new ArrayList<>();
        uids.add("   ");
        Map<String, List<String>> map = new HashMap<>();
        map.put("uid", uids);

        try {
            userBuilder.make(map);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(UsernameNotFoundException.class));
            assertThat(e.getMessage(), containsString("uid is empty"));
        }
    }

    @Ignore
    @Test
    public void testUidEmpty() {
        Map<String, String> map = new HashMap<>();
        map.put("uid", "");

        try {
            userBuilder.make(map);
            fail("Should not reach here.");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(UsernameNotFoundException.class));
            assertThat(e.getMessage(), containsString("uid is empty"));
        }
    }

    @Test(expected = UsernameNotFoundException.class)
    public void make() {
        userBuilder.make(new HashMap<String, String>());
    }
}
