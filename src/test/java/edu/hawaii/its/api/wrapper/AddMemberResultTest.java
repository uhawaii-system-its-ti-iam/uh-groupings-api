package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AddMemberResultTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        assertNotNull(new AddMemberResult(null, null));
        assertNotNull(new AddMemberResult());
        AddMemberResult addMemberResult = groupingsTestConfiguration.addMemberResultSuccessTestData();
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
    }

}
