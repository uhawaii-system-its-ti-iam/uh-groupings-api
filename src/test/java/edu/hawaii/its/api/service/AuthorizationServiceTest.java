package edu.hawaii.its.api.service;

import edu.hawaii.its.api.access.AuthorizationService;
import edu.hawaii.its.api.access.RoleHolder;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("localTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SpringBootWebApplication.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

public class AuthorizationServiceTest {

    @Autowired
    private AuthorizationService authorizationService;


    @Test
    public void fetchRolesTest() {
        String uhUuid = "0000";
        String username = "username";
        //Also tests isOwner() and isAdmin fucntions as they are called within fetchRoles.

        RoleHolder result = authorizationService.fetchRoles(uhUuid, username);
        assertTrue(result != null);
    }
}
