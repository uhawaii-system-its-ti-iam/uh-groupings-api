package edu.hawaii.its.api.wrapper;

import static edu.hawaii.its.api.service.ResponseCode.SUCCESS;
import static edu.hawaii.its.api.service.ResponseCode.SUCCESS_WASNT_IMMEDIATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.RemoveMemberRequestRejectedException;
import edu.hawaii.its.api.service.GrouperApiService;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestRemoveMemberRequest {

    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;

    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void constructor() {
        AddMemberRequest addMemberRequest = new AddMemberRequest();
        assertNotNull(addMemberRequest);
    }

    @Test
    public void sendTest() {
        RemoveMemberResponse response;
        String uid = "testiam2";
        String uhUuid = TEST_UH_NUMBERS.get(0);

        // Should not throw exception when valid uh identifiers are passed.
        try {
            // Remove uid.
            grouperApiService.addMember(GROUPING_INCLUDE, uid);
            response = new RemoveMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS, response.resultCode());

            // Remove uid that is not in the group.
            response = new RemoveMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertFalse(response.isSuccess());
            assertEquals(SUCCESS_WASNT_IMMEDIATE, response.resultCode());

            // Remove uhUuid.
            grouperApiService.addMember(GROUPING_INCLUDE, uhUuid);
            response = new RemoveMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS, response.resultCode());

            // Remove uhUuid that is not in the group.
            response = new RemoveMemberRequest(GROUPING_INCLUDE, uhUuid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertFalse(response.isSuccess());
            assertEquals(SUCCESS_WASNT_IMMEDIATE, response.resultCode());
        } catch (RemoveMemberRequestRejectedException e) {
            fail("Should not throw exception when valid uh identifiers are passed");
        }

        // Should not throw exception when invalid uh identifiers are passed.
        String bogusIdentifier = "bogus-identifier";
        try {
            // Remove uid.
            response = new RemoveMemberRequest(GROUPING_INCLUDE, bogusIdentifier).send();
            assertNotNull(response);
            assertEquals("", response.uid());
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertFalse(response.isSuccess());
            assertEquals(SUCCESS_WASNT_IMMEDIATE, response.resultCode());
        } catch (RemoveMemberRequestRejectedException e) {
            fail("Should not throw exception when invalid uh identifiers are passed");
        }

        // Should throw an exception if an invalid group path is passed.
        String badPath = "bad-path";
        try {
            response = new RemoveMemberRequest(badPath, uid).send();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (RemoveMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }
    }
}
