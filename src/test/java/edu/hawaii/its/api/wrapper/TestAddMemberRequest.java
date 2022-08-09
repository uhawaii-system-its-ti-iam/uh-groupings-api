package edu.hawaii.its.api.wrapper;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AddMemberRequestRejectedException;
import edu.hawaii.its.api.service.GrouperApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMemberRequest {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    final static private String SUCCESS = "SUCCESS";
    final static private String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";

    @Autowired
    private GrouperApiService grouperApiService;

    @Test
    public void constructor() {
        AddMemberRequest addMemberRequest = new AddMemberRequest();
        assertNotNull(addMemberRequest);
    }

    @Test
    public void sendTest() {
        AddMemberResponse response;
        String uid = "testiam2";
        String uhUuid = TEST_UH_NUMBERS.get(0);

        // Should not throw exception when valid uh identifiers are passed.
        try {
            // Add uid.
            grouperApiService.removeMember(GROUPING_INCLUDE, uid);
            response = new AddMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS, response.resultCode());

            // Add uid that has already been added.
            response = new AddMemberRequest(GROUPING_INCLUDE, uid).send();
            assertNotNull(response);
            assertEquals(uid, response.uid());
            assertEquals(uhUuid, response.uhUuid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertFalse(response.isSuccess());
            assertEquals(SUCCESS_ALREADY_EXISTED, response.resultCode());

            // Clean up
            grouperApiService.removeMember(GROUPING_INCLUDE, uid);

            // Add uhUuid.
            response = new AddMemberRequest(GROUPING_INCLUDE, uhUuid).send();
            assertNotNull(response);
            assertEquals(uhUuid, response.uhUuid());
            assertEquals(uid, response.uid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertTrue(response.isSuccess());
            assertEquals(SUCCESS, response.resultCode());

            // Add uhUuid that has already been added.
            response = new AddMemberRequest(GROUPING_INCLUDE, uhUuid).send();
            assertNotNull(response);
            assertEquals(uhUuid, response.uhUuid());
            assertEquals(uid, response.uid());
            assertFalse(response.uid().equals(response.uhUuid()));
            assertEquals(GROUPING_INCLUDE, response.groupPath());
            assertFalse(response.isSuccess());
            assertEquals(SUCCESS_ALREADY_EXISTED, response.resultCode());

            grouperApiService.removeMember(GROUPING_INCLUDE, uhUuid);

        } catch (AddMemberRequestRejectedException e) {
            fail("Should not throw exception when valid uh identifiers are passed");
        }

        // Should throw an exception if an invalid uh identifier is passed.
        String bogusIdentifier = "bogus-ident";
        try {
            response = new AddMemberRequest(GROUPING_INCLUDE, bogusIdentifier).send();
            fail("Should throw an exception if an invalid uh identifier is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

        // Should throw an exception if an invalid group path is passed.
        String badPath = "bad-path";
        try {
            response = new AddMemberRequest(badPath, uid).send();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

    }
}
