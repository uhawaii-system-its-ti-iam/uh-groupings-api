package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private GetMembersResult directOwnersResult;

    @Mock
    private GetMembersResult ownerGroupingMembersResult;

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
        Subject requestor = subject(CURRENT_USER, "Requestor Name", "11111111", "requestor@hawaii.edu");
        Subject directOwner = subject("ownerone", "Owner One", "22222222", "owner-one@hawaii.edu");
        Subject duplicateOwner = subject("duplicated", "Duplicated Owner", "33333333", "duplicate@hawaii.edu");
        Subject ownerGrouping = subject("", "hawaii.edu:custom:test:owner-grouping", "", "");
        Subject indirectOwner = subject("ownertwo", "Owner Two", "44444444", "owner-two@hawaii.edu");
        Subject indirectDuplicateOwner = subject("duplicated", "Duplicated Owner", "33333333", "duplicate@hawaii.edu");

        given(memberService.isCurrentUserAdmin()).willReturn(false);
        given(memberService.isOwner(GROUPING_PATH, CURRENT_USER)).willReturn(true);
        given(grouperService.getSubjects(CURRENT_USER)).willReturn(requestorSubjectsResults);
        given(requestorSubjectsResults.getSubjects()).willReturn(List.of(requestor));
        given(grouperService.findGroupsResults(CURRENT_USER, GROUPING_PATH)).willReturn(findGroupsResults);
        given(findGroupsResults.getGroup()).willReturn(group);
        given(group.getDescription()).willReturn("Changing description test");
        given(grouperService.getImmediateMembers(CURRENT_USER, GROUPING_PATH + ":owners"))
                .willReturn(directOwnersResult);
        given(directOwnersResult.getSubjects()).willReturn(List.of(directOwner, duplicateOwner, ownerGrouping));
        given(grouperService.getImmediateMembers(CURRENT_USER, "hawaii.edu:custom:test:owner-grouping"))
                .willReturn(ownerGroupingMembersResult);
        given(ownerGroupingMembersResult.getSubjects()).willReturn(List.of(indirectOwner, indirectDuplicateOwner));

        RetireGroupingResult result = retireGroupingService.retireGrouping(CURRENT_USER, GROUPING_PATH);

        assertEquals("SUCCESS", result.getResultCode());
        assertTrue(result.getResultMessage().contains("Notifications sent"));

        ArgumentCaptor<List<String>> ownerEmailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(emailService).sendRetireGroupingEmails(
                eq(GROUPING_PATH),
                eq(CURRENT_USER),
                eq("requestor@hawaii.edu"),
                eq("JTTEST-L"),
                eq("Changing description test"),
                ownerEmailsCaptor.capture(),
                eq("Requestor Name"));

        List<String> ownerEmails = ownerEmailsCaptor.getValue();
        assertEquals(3, ownerEmails.size());
        assertTrue(ownerEmails.contains("owner-one@hawaii.edu"));
        assertTrue(ownerEmails.contains("owner-two@hawaii.edu"));
        assertTrue(ownerEmails.contains("duplicate@hawaii.edu"));
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
}
