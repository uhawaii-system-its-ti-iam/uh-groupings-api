package edu.hawaii.its.api.access;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.controller.WithMockUhUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UserContextServiceTest {

    @Autowired
    private UserContextService userContextService;

    @Test
    @WithMockUhUser(username = "admin", roles = { "ROLE_ADMIN" })
    public void basics() {
        assertThat(userContextService.getCurrentUhUuid(), equalTo("12345678L"));
        assertThat(userContextService.getCurrentUsername(), equalTo("admin"));
        assertThat(userContextService.toString(), startsWith("UserContextServiceImpl"));

        User user = userContextService.getCurrentUser();
        assertNotNull(user);
        assertThat(user.getUhUuid(), equalTo("12345678L"));
        assertThat(user.getUsername(), equalTo("admin"));

        userContextService.setCurrentUhUuid("87654321L");
        assertThat(userContextService.getCurrentUhUuid(), equalTo("87654321L"));
    }
}
