package edu.hawaii.its.api.access;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Fail.fail;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UserDetailsServiceTest {

    @Autowired
    private UserBuilder userBuilder;

    @Test
    public void testAdminUsers() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", "duckart");
        map.put("uhUuid", "89999999");
        AttributePrincipal principal = new AttributePrincipalImpl("duckart", map);
        Assertion assertion = new AssertionImpl(principal);
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userBuilder);
        User user = (User) userDetailsService.loadUserDetails(assertion);

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

        // Check a made-up junky role name.

        map = new HashMap<>();
        map.put("uid", "someuser");
        map.put("uhUuid", "10000001");
        principal = new AttributePrincipalImpl("someuser", map);
        assertion = new AssertionImpl(principal);
        user = (User) userDetailsService.loadUserDetails(assertion);

        assertThat(user.getUsername(), is("someuser"));
        assertThat(user.getUid(), is("someuser"));
        assertThat(user.getUhUuid(), is("10000001"));

        assertTrue(user.getAuthorities().size() > 0);
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));
        assertTrue(user.isRole(Role.ADMIN));
    }

    @Test
    public void testEmployees() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", "jjcale");
        map.put("uhUuid", "10000004");

        AttributePrincipal principal = new AttributePrincipalImpl("jjcale", map);
        Assertion assertion = new AssertionImpl(principal);
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userBuilder);
        User user = (User) userDetailsService.loadUserDetails(assertion);

        // Basics.
        assertThat(user.getUsername(), is("jjcale"));
        assertThat(user.getUid(), is("jjcale"));
        assertThat(user.getUhUuid(), is("10000004"));

        // Granted Authorities.
        assertTrue(user.getAuthorities().size() == 3);
        assertTrue(user.isRole(Role.ANONYMOUS));
        assertTrue(user.isRole(Role.UH));
        assertTrue(user.isRole(Role.EMPLOYEE));

        assertFalse(user.isRole(Role.ADMIN));
    }

    @Test
    public void loadUserDetailsExceptionOne() {
        Assertion assertion = new AssertionDummy();
        UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl(userBuilder);
        try {
            userDetailsService.loadUserDetails(assertion);
            fail("Should not have reached here.");
        } catch (Exception e) {
            assertThat(UsernameNotFoundException.class, equalTo(e.getClass()));
            assertThat(e.getMessage(), containsString("principal is null"));
        }
    }
}
