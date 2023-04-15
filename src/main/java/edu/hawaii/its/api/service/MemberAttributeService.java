package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.exception.AccessDeniedException;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static edu.hawaii.its.api.service.PathFilter.parentGroupingPath;
import static edu.hawaii.its.api.service.PathFilter.pathHasOwner;

@Service("memberAttributeService")
public class MemberAttributeService {

    public static final Log logger = LogFactory.getLog(MemberAttributeService.class);

    private static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    @Value("${groupings.api.is_member}")
    private String IS_MEMBER;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsService groupingsService;

    /**
     * Return true if username is a UH id number
     */
    public boolean isUhUuid(String uhIdentifier) {
        return uhIdentifier != null && uhIdentifier.matches("\\d+");
    }

    // Returns true if the user is in the apps group
    public boolean isApp(String uhIdentifier) {
        return memberService.isMember(GROUPING_APPS, uhIdentifier);
    }

    /**
     * Get a list of invalid uhIdentifiers given a list of uhIdentifiers.
     * Returns an empty list if all uhIdentifiers are valid.
     */
    public List<String> invalidUhIdentifiers(String currentUser, List<String> uhIdentifiers) {
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }

        List<String> invalid = uhIdentifiers.parallelStream()
                .filter(uhIdentifier -> !subjectService.isValidIdentifier(uhIdentifier))
                .collect(Collectors.toList());

        return invalid;
    }

    /**
     * Get a mapping of all user attributes (uid, composite name, last name, first name, uhUuid) pertaining to the uid
     * or uhUuid passed through uhIdentifier. Passing an invalid uhIdentifier or current user will return a mapping
     * with null values.
     */
    public Person getMemberAttributes(String currentUser, String uhIdentifier) {
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }
        return subjectService.getPerson(uhIdentifier);
    }

    /**
     * Get a mapping of user attributes (composite name, uid, uhUuid) pertaining to the list of uid
     * or uhUuid passed through uhIdentifiers. Passing a single invalid uhIdentifier or current user will return an
     * empty array
     */
    public List<Subject> getMembersAttributes(String currentUser, List<String> uhIdentifiers) {
        if (!memberService.isAdmin(currentUser) && !memberService.isOwner(currentUser)) {
            throw new AccessDeniedException();
        }

        SubjectsResults results = grouperApiService.getSubjects(uhIdentifiers);

        if (results.getResultCode().equals(FAILURE)) {
            return new ArrayList<>();
        }

        List<Subject> subjects = results.getSubjects();

        return subjects;
    }

    /**
     * Get a list of GroupPaths the user owns, by username or uhUuid.
     */
    public List<GroupingPath> getOwnedGroupings(String currentUser, String uhIdentifier) {
        List<String> pathStrings = groupingsService.groupPaths(uhIdentifier, pathHasOwner());

        List<GroupingPath> groupingPaths = new ArrayList<>();
        for (String path : pathStrings) {
            String parentGroupingPath = parentGroupingPath(path);
            groupingPaths.add(new GroupingPath(parentGroupingPath,
                    groupingsService.getGroupingDescription(parentGroupingPath)));
        }

        return groupingPaths;
    }

    /**
     * Get the number of groupings a user owns, by username or uhUuid.
     */
    public Integer numberOfGroupings(String currentUser, String uhIdentifier) {
        return groupingsService.groupPaths(uhIdentifier, pathHasOwner()).size();
    }
}
