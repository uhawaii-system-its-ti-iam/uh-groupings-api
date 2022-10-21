package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FetchesProperties;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GroupingsAddResultsTest extends FetchesProperties {

    @Test
    public void test() {
        String json = propertyValue("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        GroupingsAddResults groupingsAddResults = new GroupingsAddResults(addMembersResults);
        assertNotNull(groupingsAddResults);
        List<GroupingsAddResult> results = groupingsAddResults.getResults();
        assertNotNull(results);
    }
}
