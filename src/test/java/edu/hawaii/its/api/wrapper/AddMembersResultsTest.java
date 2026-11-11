package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class AddMembersResultsTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.test.uh-uuids}")
    private List<String> TEST_UH_UUIDS;

    @Value("${groupings.api.test.uh-names}")
    private List<String> TEST_NAMES;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void construction() {
        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);
        assertNotNull(new AddMembersResults(null));
        assertNotNull(new AddMembersResults());
    }

    @Test
    public void test() {
        AddMembersResults addMembersResults = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertNotNull(addMembersResults);
        assertEquals("SUCCESS", addMembersResults.getResultCode());
        assertEquals("group-path", addMembersResults.getGroupPath());
        List<AddMemberResult> addMemberResults = addMembersResults.getResults();
        assertNotNull(addMemberResults);
        assertEquals(5, addMemberResults.size());
        AddMemberResult addMemberResult = addMemberResults.get(0);
        assertEquals("SUCCESS_ALREADY_EXISTED", addMemberResult.getResultCode());
        assertEquals("group-path", addMemberResult.getGroupPath());
        assertEquals(TEST_UH_UUIDS.get(0), addMemberResult.getUhUuid());
        assertEquals(TEST_UIDS.get(0), addMemberResult.getUid());
        assertEquals(TEST_NAMES.get(0), addMemberResult.getName());
    }

    @Test
    public void mergeOfOneBatchIsThatBatch() {
        AddMembersResults batch = groupingsTestConfiguration.addMemberResultsSuccessTestData();
        assertSame(batch, AddMembersResults.merge(List.of(batch)));
    }

    @Test
    public void mergeOfNoBatchesIsEmpty() {
        assertTrue(AddMembersResults.merge(List.of()).getResults().isEmpty());
    }

    @Test
    public void mergeCombinesTheBatchesInOrder() {
        AddMembersResults merged = AddMembersResults.merge(List.of(
                batch("SUCCESS", "00000001", "00000002"),
                batch("SUCCESS", "00000003")));

        assertEquals("SUCCESS", merged.getResultCode());
        assertEquals("group-path", merged.getGroupPath());
        assertEquals(List.of("00000001", "00000002", "00000003"),
                merged.getResults().stream().map(AddMemberResult::getUhUuid).toList());
        assertEquals("group-path", merged.getResults().get(2).getGroupPath());
    }

    @Test
    public void mergeReportsAFailedBatch() {
        AddMembersResults merged = AddMembersResults.merge(List.of(
                batch("SUCCESS", "00000001"),
                batch("FAILURE", "00000002"),
                batch("SUCCESS", "00000003")));

        assertEquals("FAILURE", merged.getResultCode());
        assertEquals(3, merged.getResults().size());
    }

    private AddMembersResults batch(String resultCode, String... uhUuids) {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("group-path");
        WsAddMemberResults wsAddMemberResults = new WsAddMemberResults();
        wsAddMemberResults.setWsGroupAssigned(wsGroup);
        wsAddMemberResults.setResultMetadata(resultMetadata(resultCode));
        wsAddMemberResults.setResults(Arrays.stream(uhUuids).map(uhUuid -> {
            WsSubject wsSubject = new WsSubject();
            wsSubject.setId(uhUuid);
            WsAddMemberResult wsAddMemberResult = new WsAddMemberResult();
            wsAddMemberResult.setWsSubject(wsSubject);
            wsAddMemberResult.setResultMetadata(resultMetadata("SUCCESS"));
            return wsAddMemberResult;
        }).toArray(WsAddMemberResult[]::new));
        return new AddMembersResults(wsAddMemberResults);
    }

    private WsResultMeta resultMetadata(String resultCode) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(resultCode);
        return resultMetadata;
    }
}
