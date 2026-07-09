package edu.hawaii.its.api.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.RetireGroupingResult;
import edu.hawaii.its.api.wrapper.GetMembersResult;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

/**
 * Handles grouping retirement requests and owner notification lookup.
 */
@Service
public class RetireGroupingService {

    private static final Log logger = LogFactory.getLog(RetireGroupingService.class);

    private static final int EMAIL_ATTRIBUTE_INDEX = 4;

    private final GrouperService grouperService;

    private final EmailService emailService;

    private final MemberService memberService;

    public RetireGroupingService(GrouperService grouperService, EmailService emailService, MemberService memberService) {
        this.grouperService = grouperService;
        this.emailService = emailService;
        this.memberService = memberService;
    }

    public RetireGroupingResult retireGrouping(String currentUser, String groupingPath) {
        if (!memberService.isCurrentUserAdmin() && !memberService.isOwner(groupingPath, currentUser)) {
            throw new AccessDeniedException();
        }

        String groupingName = extractGroupingName(groupingPath);
        String groupingDescription = getGroupingDescription(currentUser, groupingPath);
        Subject requestor = getSubject(currentUser);
        String requestorName = !requestor.getName().isEmpty() ? requestor.getName() : currentUser;
        String requestorEmail = getEmail(requestor);
        List<String> ownerEmails = new ArrayList<>(gatherOwnerEmails(currentUser, groupingPath));

        emailService.sendRetireGroupingEmails(
                groupingPath,
                currentUser,
                requestorEmail,
                groupingName,
                groupingDescription,
                ownerEmails,
                requestorName);

        return new RetireGroupingResult("SUCCESS",
                "Retire request processed successfully. Notifications sent to IAM team and grouping owners.");
    }

    private Set<String> gatherOwnerEmails(String currentUser, String groupingPath) {
        Set<String> ownerEmails = new HashSet<>();
        String ownersGroupPath = groupingPath + GroupType.OWNERS.value();
        GetMembersResult ownersResult = grouperService.getImmediateMembers(currentUser, ownersGroupPath);

        for (Subject owner : ownersResult.getSubjects()) {
            if (isOwnerGrouping(owner)) {
                ownerEmails.addAll(gatherOwnerGroupingMemberEmails(currentUser, owner.getName()));
                continue;
            }

            String email = getEmail(owner);
            if (!email.isEmpty()) {
                ownerEmails.add(email);
            }
        }

        return ownerEmails;
    }

    private Set<String> gatherOwnerGroupingMemberEmails(String currentUser, String ownerGroupingPath) {
        Set<String> emails = new HashSet<>();
        GetMembersResult ownerGroupingMembers = grouperService.getImmediateMembers(currentUser, ownerGroupingPath);

        for (Subject member : ownerGroupingMembers.getSubjects()) {
            String email = getEmail(member);
            if (!email.isEmpty()) {
                emails.add(email);
            }
        }

        return emails;
    }

    private boolean isOwnerGrouping(Subject subject) {
        return subject.getName().contains(":");
    }

    private String getEmail(Subject subject) {
        String email = subject.getAttributeValue(EMAIL_ATTRIBUTE_INDEX);
        if (!email.isEmpty()) {
            return email;
        }
        return lookupEmail(subject.getUid());
    }

    private String lookupEmail(String uid) {
        if (uid == null || uid.isEmpty()) {
            return "";
        }

        Subject subject = getSubject(uid);
        return subject.getAttributeValue(EMAIL_ATTRIBUTE_INDEX);
    }

    private Subject getSubject(String uid) {
        try {
            SubjectsResults subjectsResults = grouperService.getSubjects(uid);
            List<Subject> subjects = subjectsResults.getSubjects();
            if (!subjects.isEmpty()) {
                return subjects.get(0);
            }
        } catch (Exception e) {
            logger.debug("Unable to look up subject for uid: " + uid, e);
        }
        return new Subject();
    }

    private String getGroupingDescription(String currentUser, String groupingPath) {
        try {
            return grouperService.findGroupsResults(currentUser, groupingPath).getGroup().getDescription();
        } catch (Exception e) {
            logger.debug("Unable to look up grouping description for path: " + groupingPath, e);
            return "";
        }
    }

    private String extractGroupingName(String groupingPath) {
        if (groupingPath == null || groupingPath.isEmpty()) {
            return "";
        }

        String[] pathParts = groupingPath.split(":");
        return pathParts[pathParts.length - 1];
    }
}
