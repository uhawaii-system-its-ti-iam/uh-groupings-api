package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;

import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

public class RemoveMembersResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void construction() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        removeMembersResults = new RemoveMembersResults(null);
        assertNotNull(removeMembersResults);
    }

    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        assertEquals("SUCCESS", removeMembersResults.getResultCode());
        assertEquals("group-path", removeMembersResults.getGroupPath());
        assertNotNull(removeMembersResults.getResults());
        assertEquals(5, removeMembersResults.getResults().size());
    }

    @Test
    public void mergeOfOneBatchIsThatBatch() {
        RemoveMembersResults batch = new RemoveMembersResults(
                JsonUtil.asObject(propertyValue("ws.delete.member.results.success"), WsDeleteMemberResults.class));
        assertSame(batch, RemoveMembersResults.merge(List.of(batch)));
    }

    @Test
    public void mergeOfNoBatchesIsEmpty() {
        assertTrue(RemoveMembersResults.merge(List.of()).getResults().isEmpty());
    }

    @Test
    public void mergeCombinesTheBatchesInOrder() {
        RemoveMembersResults merged = RemoveMembersResults.merge(List.of(
                batch("SUCCESS", "00000001", "00000002"),
                batch("SUCCESS", "00000003")));

        assertEquals("SUCCESS", merged.getResultCode());
        assertEquals("group-path", merged.getGroupPath());
        assertEquals(List.of("00000001", "00000002", "00000003"),
                merged.getResults().stream().map(RemoveMemberResult::getUhUuid).toList());
        assertEquals("group-path", merged.getResults().get(2).getGroupPath());
    }

    @Test
    public void mergeReportsAFailedBatch() {
        RemoveMembersResults merged = RemoveMembersResults.merge(List.of(
                batch("SUCCESS", "00000001"),
                batch("FAILURE", "00000002"),
                batch("SUCCESS", "00000003")));

        assertEquals("FAILURE", merged.getResultCode());
        assertEquals(3, merged.getResults().size());
    }

    private RemoveMembersResults batch(String resultCode, String... uhUuids) {
        WsGroup wsGroup = new WsGroup();
        wsGroup.setName("group-path");
        WsDeleteMemberResults wsDeleteMemberResults = new WsDeleteMemberResults();
        wsDeleteMemberResults.setWsGroup(wsGroup);
        wsDeleteMemberResults.setResultMetadata(resultMetadata(resultCode));
        wsDeleteMemberResults.setResults(Arrays.stream(uhUuids).map(uhUuid -> {
            WsSubject wsSubject = new WsSubject();
            wsSubject.setId(uhUuid);
            WsDeleteMemberResult wsDeleteMemberResult = new WsDeleteMemberResult();
            wsDeleteMemberResult.setWsSubject(wsSubject);
            wsDeleteMemberResult.setResultMetadata(resultMetadata("SUCCESS"));
            return wsDeleteMemberResult;
        }).toArray(WsDeleteMemberResult[]::new));
        return new RemoveMembersResults(wsDeleteMemberResults);
    }

    private WsResultMeta resultMetadata(String resultCode) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode(resultCode);
        return resultMetadata;
    }

    public List<String> getTestUids() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestUhUuids() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNames() {
        String[] array = { "Testf-iwt-a TestIAM-staff", "Testf-iwt-b TestIAM-staff", "Testf-iwt-c TestIAM-staff",
                "Testf-iwt-d TestIAM-faculty", "Testf-iwt-e TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestFirstNames() {
        String[] array = { "Testf-iwt-a", "Testf-iwt-b", "Testf-iwt-c", "Testf-iwt-d", "Testf-iwt-e" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestLastNames() {
        String[] array = { "TestIAM-staff", "TestIAM-staff", "TestIAM-staff", "TestIAM-faculty", "TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
