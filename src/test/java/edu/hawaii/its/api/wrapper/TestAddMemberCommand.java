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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@ActiveProfiles("integrationTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestAddMemberCommand {
    @Value("${groupings.api.test.uhuuids}")
    private List<String> TEST_UH_NUMBERS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;

    final static private String SUCCESS = "SUCCESS";
    final static private String SUCCESS_ALREADY_EXISTED = "SUCCESS_ALREADY_EXISTED";

    @Autowired
    private GrouperApiService grouperApiService;

    @Test public void constructor() {
        AddMemberCommand addMemberCommand = new AddMemberCommand(GROUPING_INCLUDE, TEST_UH_NUMBERS.get(0));
        assertNotNull(addMemberCommand);
    }

    @Test
    public void executeTest() {
        AddMemberResult addMemberResult;
        String uid = "testiam2";
        String uhUuid = TEST_UH_NUMBERS.get(0);

        // Should throw an exception if an invalid uh identifier is passed.
        String bogusIdentifier = "bogus-ident";
        try {
            addMemberResult = new AddMemberCommand(GROUPING_INCLUDE, "bogus-ident").execute();
            fail("Should throw an exception if an invalid uh identifier is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

        // Should throw an exception if an invalid group path is passed.
        String badPath = "bad-path";
        try {
            addMemberResult = new AddMemberCommand(badPath, TEST_UH_NUMBERS.get(0)).execute();
            fail("Should throw an exception if an invalid group path is passed.");
        } catch (AddMemberRequestRejectedException e) {
            assertNull(e.getCause());
        }

    }
}
