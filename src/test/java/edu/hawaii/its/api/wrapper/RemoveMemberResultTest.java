package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

public class RemoveMemberResultTest {

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
        assertNotNull(wsDeleteMemberResults);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertNotNull(removeMemberResult);
        assertNotNull(new RemoveMemberResult());
        assertNotNull(new RemoveMemberResult(null, null));

    }

    @Test
    public void test() {
        String json = propertyValue("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertEquals("SUCCESS_WASNT_IMMEDIATE", removeMemberResult.getResultCode());
        assertEquals("group-path", removeMemberResult.getGroupPath());
        assertEquals(getTestNumbers().get(0), removeMemberResult.getUhUuid());
        assertEquals(getTestUids().get(0), removeMemberResult.getUid());
        assertEquals(getTestNames().get(0), removeMemberResult.getName());
        assertNotNull(removeMemberResult.getSubject());
    }


    public List<String> getTestUids() {
        String[] array = { "testiwta", "testiwtb", "testiwtc", "testiwtd", "testiwte" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNumbers() {
        String[] array = { "99997010", "99997027", "99997033", "99997043", "99997056" };
        return new ArrayList<>(Arrays.asList(array));
    }

    public List<String> getTestNames() {
        String[] array = { "Testf-iwt-a TestIAM-staff", "Testf-iwt-b TestIAM-staff", "Testf-iwt-c TestIAM-staff",
                "Testf-iwt-d TestIAM-faculty", "Testf-iwt-e TestIAM-student" };
        return new ArrayList<>(Arrays.asList(array));
    }

    private String propertyValue(String key) {
        return properties.getProperty(key);
    }
}
