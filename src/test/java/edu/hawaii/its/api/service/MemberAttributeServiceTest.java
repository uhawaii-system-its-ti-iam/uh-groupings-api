package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.groupings.MemberAttributeResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsResultMeta;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberAttributeServiceTest {

    private static final String TEST_USER = "testiwta";

    @MockitoBean
    private GrouperService grouperService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @BeforeEach
    public void setUpSecurityContext() {
        setRoles("ROLE_ADMIN");
    }

    @AfterEach
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setRoles(String... roles) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                TEST_USER,
                null,
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList()));
        SecurityContextHolder.setContext(context);
    }

    /**
     * Builds a SubjectsResults with one WsSubject per identifier, in request order. A found subject carries
     * UH attributes, like a real Grouper lookup, so it is kept when member attributes are read back.
     */
    private SubjectsResults subjectsResultsInOrder(List<String> identifiers, List<String> resultCodes) {
        WsResultMeta resultMetadata = new WsResultMeta();
        resultMetadata.setResultCode("SUCCESS");

        WsSubject[] wsSubjects = new WsSubject[identifiers.size()];
        for (int i = 0; i < identifiers.size(); i++) {
            WsSubject subject = new WsSubject();
            subject.setResultCode(resultCodes.get(i));
            subject.setIdentifierLookup(identifiers.get(i));
            if (resultCodes.get(i).equals("SUCCESS")) {
                subject.setId("uhuuid-" + identifiers.get(i));
                subject.setAttributeValues(new String[] { identifiers.get(i), "Name", "Last", "First", "email" });
            }
            wsSubjects[i] = subject;
        }

        WsGetSubjectsResults wsGetSubjectsResults = new WsGetSubjectsResults();
        wsGetSubjectsResults.setResultMetadata(resultMetadata);
        wsGetSubjectsResults.setWsSubjects(wsSubjects);
        return new SubjectsResults(wsGetSubjectsResults);
    }

    @Test
    public void construction() {
        assertNotNull(memberAttributeService);
    }

    @Test
    public void getMemberAttributeResultsReportsMalformedAndUnknownIdentifiersAsInvalid() {
        List<String> identifiers = List.of("12-345-678", "00000001", "99999999999", "!!!!!!!!");
        List<String> wellFormed = List.of("00000001", "99999999999");
        given(grouperService.getSubjects(wellFormed))
                .willReturn(subjectsResultsInOrder(wellFormed, List.of("SUCCESS", "SUBJECT_NOT_FOUND")));

        MemberAttributeResults results = memberAttributeService.getMemberAttributeResults(TEST_USER, identifiers);

        assertEquals(List.of("12-345-678", "99999999999", "!!!!!!!!"), results.getInvalid());
        assertTrue(results.getResults().isEmpty());
        assertEquals("FAILURE", results.getResultCode());
        // One bulk lookup for the whole list, not one Grouper call per identifier.
        verify(grouperService, times(1)).getSubjects(anyList());
        verify(grouperService, never()).getSubjects(anyString());
    }

    @Test
    public void getMemberAttributeResultsAsyncReportsMalformedAndUnknownIdentifiersAsInvalid() {
        List<String> identifiers = List.of("12-345-678", "00000001", "99999999999", "!!!!!!!!");
        List<String> wellFormed = List.of("00000001", "99999999999");
        given(grouperService.getSubjects(wellFormed))
                .willReturn(subjectsResultsInOrder(wellFormed, List.of("SUCCESS", "SUBJECT_NOT_FOUND")));

        MemberAttributeResults results =
                memberAttributeService.getMemberAttributeResultsAsync(TEST_USER, identifiers).join();

        assertEquals(List.of("12-345-678", "99999999999", "!!!!!!!!"), results.getInvalid());
        assertTrue(results.getResults().isEmpty());
        verify(grouperService, times(1)).getSubjects(anyList());
        verify(grouperService, never()).getSubjects(anyString());
    }

    @Test
    public void getMemberAttributeResultsWithOnlyMalformedIdentifiersDoesNotQueryGrouper() {
        List<String> identifiers = List.of("12-345-678", "!!!!!!!!");

        MemberAttributeResults results =
                memberAttributeService.getMemberAttributeResultsAsync(TEST_USER, identifiers).join();

        assertEquals(identifiers, results.getInvalid());
        assertTrue(results.getResults().isEmpty());
        verifyNoInteractions(grouperService);
    }

    @Test
    public void getMemberAttributeResultsReportsARepeatedInvalidIdentifierOnce() {
        List<String> identifiers = List.of("1234", "00000001", "1234", "00000001");
        List<String> unique = List.of("1234", "00000001");
        given(grouperService.getSubjects(unique))
                .willReturn(subjectsResultsInOrder(unique, List.of("SUBJECT_NOT_FOUND", "SUCCESS")));

        MemberAttributeResults results = memberAttributeService.getMemberAttributeResults(TEST_USER, identifiers);

        assertEquals(List.of("1234"), results.getInvalid());
    }

    @Test
    public void getMemberAttributeResultsReturnsAttributesWhenEveryIdentifierIsValid() {
        List<String> identifiers = List.of("00000001", "00000002");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(identifiers, List.of("SUCCESS", "SUCCESS")));

        MemberAttributeResults results = memberAttributeService.getMemberAttributeResults(TEST_USER, identifiers);

        assertTrue(results.getInvalid().isEmpty());
        assertEquals("SUCCESS", results.getResultCode());
        assertEquals(2, results.getResults().size());
    }

    @Test
    public void getMemberAttributeResultsAllowsOwnerWhoIsNotAdmin() {
        setRoles("ROLE_OWNER");
        List<String> identifiers = List.of("00000001");
        given(grouperService.getSubjects(identifiers))
                .willReturn(subjectsResultsInOrder(identifiers, List.of("SUCCESS")));

        MemberAttributeResults results = memberAttributeService.getMemberAttributeResults(TEST_USER, identifiers);

        assertEquals(1, results.getResults().size());
    }

    @Test
    public void getMemberAttributeResultsRequiresAdminOrOwner() {
        setRoles("ROLE_UH");
        List<String> identifiers = List.of("00000001");

        assertThrows(AccessDeniedException.class,
                () -> memberAttributeService.getMemberAttributeResults(TEST_USER, identifiers));
        // An async failure surfaces from the returned future, wrapped in a CompletionException.
        CompletionException asyncFailure = assertThrows(CompletionException.class,
                () -> memberAttributeService.getMemberAttributeResultsAsync(TEST_USER, identifiers).join());
        assertInstanceOf(AccessDeniedException.class, asyncFailure.getCause());
        verifyNoInteractions(grouperService);
    }
}
