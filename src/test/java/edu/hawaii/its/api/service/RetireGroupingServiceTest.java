package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.RetireGroupingResult;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@ExtendWith(MockitoExtension.class)
public class RetireGroupingServiceTest {

    private static final String CURRENT_USER = "rainem";

    private static final String GROUPING_PATH = "hawaii.edu:custom:test:listserv-tests:JTTEST-L";

    private RetireGroupingService retireGroupingService;

    @Mock
    private GrouperService grouperService;

    @Mock
    private EmailService emailService;

    @Mock
    private MemberService memberService;

    @Mock
    private SubjectsResults requestorSubjectsResults;

    @Mock
    private FindGroupsResults findGroupsResults;

    @Mock
    private edu.hawaii.its.api.wrapper.Group group;

    @BeforeEach
    public void setUp() {
        retireGroupingService = new RetireGroupingService(grouperService, emailService, memberService);
    }

    @Test
    public void retireGrouping() {
        Subject requestor = subject(CURRENT_USER, "Requestor Name", "11111111", "Requestor Name");
        WsGroup ownersGroup = new WsGroup();
        ownersGroup.setName(GROUPING_PATH + ":owners");
        WsGetMembersResult wsOwners = new WsGetMembersResult();
        wsOwners.setWsGroup(ownersGroup);
        wsOwners.setWsSubjects(new WsSubject[] {
                wsSubject("ownerone", "Owner One", "22222222", "UH core LDAP"),
                wsSubject("duplicated", "Duplicated Owner", "33333333", "UH core LDAP"),
                wsSubject("", "hawaii.edu:custom:test:owner-grouping", "group-id", "g:gsa"),
                wsSubject("ownertwo", "Owner Two", "44444444", "UH core LDAP"),
                wsSubject("duplicated", "Duplicated Owner", "33333333", "UH core LDAP"),
                wsSubject("nested-owner-grouping", "Nested owner grouping", "group-id-2", "g:gsa"),
                wsSubject("", "Owner Missing UID", "66666666", "UH core LDAP")
        });

        given(memberService.isCurrentUserAdmin()).willReturn(false);
        given(memberService.isOwner(GROUPING_PATH, CURRENT_USER)).willReturn(true);
        given(grouperService.getSubjects(CURRENT_USER)).willReturn(requestorSubjectsResults);
        given(requestorSubjectsResults.getSubjects()).willReturn(List.of(requestor));
        given(grouperService.findGroupsResults(CURRENT_USER, GROUPING_PATH)).willReturn(findGroupsResults);
        given(findGroupsResults.getGroup()).willReturn(group);
        given(group.getDescription()).willReturn("Changing description test");
        given(grouperService.getAllMembers(CURRENT_USER, GROUPING_PATH + ":owners"))
                .willReturn(new GetMembersResult(wsOwners));

        RetireGroupingResult emailResult = new RetireGroupingResult("SUCCESS",
                "Retirement request emails were sent.",
                List.of("ownerone@hawaii.edu", "duplicated@hawaii.edu", "ownertwo@hawaii.edu"));
        given(emailService.sendRetireGroupingEmails(
                eq(GROUPING_PATH), eq("rainem@hawaii.edu"), eq("JTTEST-L"),
                eq("Changing description test"), org.mockito.ArgumentMatchers.anyList(), eq("Requestor Name")))
                .willReturn(emailResult);

        RetireGroupingResult result = retireGroupingService.retireGrouping(CURRENT_USER, GROUPING_PATH);

        assertEquals("SUCCESS", result.getResultCode());

        ArgumentCaptor<List<String>> ownerEmailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(emailService).sendRetireGroupingEmails(
                eq(GROUPING_PATH),
                eq("rainem@hawaii.edu"),
                eq("JTTEST-L"),
                eq("Changing description test"),
                ownerEmailsCaptor.capture(),
                eq("Requestor Name"));

        List<String> ownerEmails = ownerEmailsCaptor.getValue();
        assertEquals(3, ownerEmails.size());
        assertTrue(ownerEmails.contains("ownerone@hawaii.edu"));
        assertTrue(ownerEmails.contains("ownertwo@hawaii.edu"));
        assertTrue(ownerEmails.contains("duplicated@hawaii.edu"));
        assertFalse(ownerEmails.contains("nested-owner-grouping@hawaii.edu"));
        assertFalse(ownerEmails.contains("@hawaii.edu"));
    }

    @Test
    public void retireGroupingAccessDenied() {
        given(memberService.isCurrentUserAdmin()).willReturn(false);
        given(memberService.isOwner(GROUPING_PATH, CURRENT_USER)).willReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> retireGroupingService.retireGrouping(CURRENT_USER, GROUPING_PATH));

        verifyNoInteractions(emailService);
    }

    private Subject subject(String uid, String name, String uhUuid, String email) {
        Subject subject = new Subject(uid, name, uhUuid);
        subject.setAttributeValue(4, email);
        return subject;
    }

    private WsSubject wsSubject(String uid, String name, String id, String sourceId) {
        WsSubject subject = new WsSubject();
        subject.setId(id);
        subject.setName(name);
        subject.setSourceId(sourceId);
        subject.setAttributeValues(new String[] { uid });
        return subject;
    }
}
