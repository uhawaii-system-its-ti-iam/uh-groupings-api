package edu.hawaii.its.api.access;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Test
    public void testUidNull() {
        List<String> uids = new ArrayList<>();
        uids.add("   ");
        Map<String, List<String>> map = new HashMap<>();
        map.put("uid", uids);

        try {
            userBuilder.make(map);
            Assert.fail("Should not reach here.");
        } catch (Exception e) {
            Assert.assertThat(UsernameNotFoundException.class, equalTo(e.getClass()));
            Assert.assertThat(e.getMessage(), containsString("uid is empty"));
        }
    }

    @Test(expected = UsernameNotFoundException.class)
    public void make() {
        userBuilder.make(new HashMap<String, String>());
    }
}
