package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.HasMemberRequestRejectedException;
import edu.hawaii.its.api.service.GrouperApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestHasMemberRequest {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    private static final String IS_MEMBER = "IS_MEMBER";

    private static final String IS_NOT_MEMBER = "IS_NOT_MEMBER";

    private String GROUP_NOT_FOUND = "GROUP_NOT_FOUND";
    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void constructor() {
        HasMemberRequest hasMemberRequest = new HasMemberRequest();
        assertNotNull(hasMemberRequest);
    }

    @Test void isAdminTest() {
        String admin = "iamtst01";
        HasMemberResponse response;

        grouperApiService.removeMember(GROUPING_ADMINS, admin);
        response = new HasMemberRequest(GROUPING_ADMINS, admin).send();
        assertFalse(response.isMember());

        grouperApiService.addMember(GROUPING_ADMINS, admin);
        response = new HasMemberRequest(GROUPING_ADMINS, admin).send();
        assertTrue(response.isMember());

        grouperApiService.removeMember(GROUPING_ADMINS, admin);
    }

    @Test
    public void sendTest() {
        HasMemberResponse response;
        String uid = "testiam2";
        String uhUuid = TEST_UH_NUMBERS.get(0);

        // Should not throw exception when valid uh identifiers are passed.
        try {
            // Non-member queried with UH username.
            grouperApiService.removeMember(GROUPING_INCLUDE, uid);
            response = new HasMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertFalse(response.isMember());
            assertEquals(IS_NOT_MEMBER, response.resultCode());

            // Member queried with UH username.
            grouperApiService.addMember(GROUPING_INCLUDE, uid);
            response = new HasMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertTrue(response.isMember());
            assertEquals(IS_MEMBER, response.resultCode());
            grouperApiService.removeMember(GROUPING_INCLUDE, uid);

            // Non-member queried with UH number.
            grouperApiService.removeMember(GROUPING_INCLUDE, uhUuid);
            response = new HasMemberRequest(GROUPING_INCLUDE, uhUuid).send();
            assertNotNull(response);
            assertFalse(response.isMember());
            assertEquals(IS_NOT_MEMBER, response.resultCode());

            // Member queried with UH number.
            grouperApiService.addMember(GROUPING_INCLUDE, uhUuid);
            response = new HasMemberRequest(GROUPING_INCLUDE, uhUuid).send();
            assertNotNull(response);
            assertTrue(response.isMember());
            assertEquals(IS_MEMBER, response.resultCode());
            grouperApiService.removeMember(GROUPING_INCLUDE, uhUuid);

        } catch (HasMemberRequestRejectedException e) {
            fail("Should not throw exception when valid uh identifiers are passed.");
        }
    }

    @Test
    public void exceptionsTest() {
        String uhUuid = TEST_UH_NUMBERS.get(0);

        // Should throw an exception if an invalid path is queried.
        String bogusPath = "bogus-path";
        try {
            new HasMemberRequest(bogusPath, uhUuid).send();
            fail("Should throw an exception if an invalid path is queried.");
        } catch (HasMemberRequestRejectedException e) {
            assertTrue(e.getMessage().contains(GROUP_NOT_FOUND));
        }
    }
}
