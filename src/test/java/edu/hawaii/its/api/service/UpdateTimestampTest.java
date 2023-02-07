package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.groupings.GroupingsAddResult;
import edu.hawaii.its.api.groupings.GroupingsAddResults;
import edu.hawaii.its.api.groupings.GroupingsRemoveResult;
import edu.hawaii.its.api.groupings.GroupingsRemoveResults;
import edu.hawaii.its.api.groupings.GroupingsReplaceGroupMembersResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UpdateTimestampTest {

    @Autowired
    private UpdateTimestampService updateTimestampService;

    @Test
    public void addResults() {
        assertNull(updateTimestampService.addResults(new GroupingsAddResults()));
    }

    @Test
    public void removeResults() {
        assertNull(updateTimestampService.removeResults(new GroupingsRemoveResults()));
    }

    @Test
    public void addResult() {
        assertNull(updateTimestampService.addResult(new GroupingsAddResult()));
    }

    @Test
    public void removeResult() {
        assertNull(updateTimestampService.removeResult(new GroupingsRemoveResult()));
    }

    @Test
    public void replaceGroupMembersResult() {
        assertNull(updateTimestampService.replaceGroupMembersResult(new GroupingsReplaceGroupMembersResult()));
    }

}
