package edu.hawaii.its.api.groupings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

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

    @Test
    public void forValidSubjectsBuildsTheSameShapeAsTheSubjectsResultsConstructor() {
        // Used when a caller (MemberAttributeService) already has the resolved Subjects from validating
        // identifiers, so it doesn't have to look them up in Grouper a second time just for their attributes.
        WsSubject wsSubject = new WsSubject();
        wsSubject.setResultCode("SUCCESS");
        wsSubject.setId("00000001");
        wsSubject.setAttributeValues(new String[] { "uidone", "Name", "Last", "First", "email" });
        Subject subject = new Subject(wsSubject);

        MemberAttributeResults memberAttributeResults = MemberAttributeResults.forValidSubjects(List.of(subject));

        assertEquals("SUCCESS", memberAttributeResults.getResultCode());
        assertEquals(1, memberAttributeResults.getResults().size());
        assertNotNull(memberAttributeResults.getInvalid());
        assertEquals(0, memberAttributeResults.getInvalid().size());
    }

    @Test
    public void forValidSubjectsReportsFailureForAnEmptyList() {
        MemberAttributeResults memberAttributeResults = MemberAttributeResults.forValidSubjects(List.of());

        assertEquals("FAILURE", memberAttributeResults.getResultCode());
        assertEquals(0, memberAttributeResults.getResults().size());
    }

    @Test
    public void forValidSubjectsExcludesASuccessfulOrphanSubjectFromResultsAndReportsFailureWhenItsTheOnlyOne() {
        // An orphan (found in Grouper, but with no LDAP attributes) is a valid member for adding to a grouping,
        // but has nothing to display here - matching what the SubjectsResults-based constructor above already
        // does for a subject like this (see SubjectsResults.getSubjects()).
        WsSubject orphan = new WsSubject();
        orphan.setResultCode("SUCCESS");
        orphan.setId("00000002");
        Subject orphanSubject = new Subject(orphan);

        MemberAttributeResults memberAttributeResults = MemberAttributeResults.forValidSubjects(List.of(orphanSubject));

        assertEquals("FAILURE", memberAttributeResults.getResultCode());
        assertEquals(0, memberAttributeResults.getResults().size());
    }

}
