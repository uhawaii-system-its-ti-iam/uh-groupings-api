package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MemberAttributeResultsTest {

    private static Properties properties;

    @BeforeAll
    public static void beforeAll() throws Exception {
        Path path = Paths.get("src/test/resources");
        Path file = path.resolve("grouper.test.properties");
        properties = new Properties();
        properties.load(new FileInputStream(file.toFile()));
    }

    @Test
    public void test() {
        String json = properties.getProperty("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        MemberAttributeResults memberAttributeResults = new MemberAttributeResults(subjectsResults);
        assertNotNull(memberAttributeResults);

        assertEquals("SUCCESS", memberAttributeResults.getResultCode());
        assertNotNull(memberAttributeResults.getResults());
        assertEquals(3, memberAttributeResults.getResults().size());

        memberAttributeResults = new MemberAttributeResults();
        assertEquals("FAILURE", memberAttributeResults.getResultCode());
        assertNotNull(memberAttributeResults.getResults());
        assertEquals(0, memberAttributeResults.getResults().size());

        List<String> invalid = Arrays.asList("Invalid1", "Invalid2");
        memberAttributeResults = new MemberAttributeResults(invalid);
        assertEquals("FAILURE", memberAttributeResults.getResultCode());
        assertNotNull(memberAttributeResults.getInvalid());
        assertEquals(invalid, memberAttributeResults.getInvalid());
    }

}
