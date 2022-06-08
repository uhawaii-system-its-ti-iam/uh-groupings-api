package edu.hawaii.its.api.service;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.access.AuthorizationService;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(classes = { SpringBootWebApplication.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

public class AuthorizationServiceTest {

    @Autowired
    private AuthorizationService authorizationService;

    @Test
    public void fetchRolesTest() {
        String uhUuid = "0000";
        String username = "username";
        //Also tests isOwner() and isAdmin fucntions as they are called within fetchRoles.

        /*
        RoleHolder result = authorizationService.fetchRoles(uhUuid, username);
        assertNotNull(result);
         */
    }
}
