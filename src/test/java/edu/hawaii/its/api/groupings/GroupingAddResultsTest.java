package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.wrapper.AddMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class GroupingAddResultsTest {

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void constructor() {
        GroupingAddResults groupingAddResults = new GroupingAddResults(
                groupingsTestConfiguration.addMemberResultsSuccessTestData());
        assertNotNull(groupingAddResults);
        List<GroupingAddResult> results = groupingAddResults.getResults();
        assertNotNull(results);
        assertEquals("SUCCESS", groupingAddResults.getResultCode());
    }

    @Test
    public void resultCodeIsSuccessWhenEveryMemberWasIdempotentlyAlreadyPresent() {
        // A batch where every member was already in the list (idempotent) must still count as a success,
        // not a failure - none of the individual results are the literal "SUCCESS" string.
        WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS_ALREADY_EXISTED");
        wsAddMemberResult.setResultMetadata(resultMetadata);

        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        wsAddMemberResults.setResults(new WsAddMemberResult[] { wsAddMemberResult, wsAddMemberResult });

        GroupingAddResults groupingAddResults = new GroupingAddResults(new AddMembersResults(wsAddMemberResults));
        assertEquals("SUCCESS", groupingAddResults.getResultCode());
    }

    @Test
    public void resultCodeIsFailureWhenNoMemberSucceeded() {
        WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("FAILURE");
        wsAddMemberResult.setResultMetadata(resultMetadata);

        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        wsAddMemberResults.setResults(new WsAddMemberResult[] { wsAddMemberResult });

        GroupingAddResults groupingAddResults = new GroupingAddResults(new AddMembersResults(wsAddMemberResults));
        assertEquals("FAILURE", groupingAddResults.getResultCode());
    }

}
