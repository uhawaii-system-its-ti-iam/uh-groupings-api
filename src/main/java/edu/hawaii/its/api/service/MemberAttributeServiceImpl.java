package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("memberAttributeService")
public class MemberAttributeServiceImpl implements MemberAttributeService {

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    @Value("${groupings.api.is_member}")
    private String IS_MEMBER;

    private static final String SUBJECT_NOT_FOUND = "SUBJECT_NOT_FOUND";

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    public static final Log logger = LogFactory.getLog(MemberAttributeServiceImpl.class);

    // Returns true if the user is a member of the group via username or UH id
    @Override
    public boolean isMember(String groupPath, String username) {
        logger.info("isMember; groupPath: " + groupPath + "; username: " + username + ";");

        if (helperService.isUhUuid(username)) {
            return isMemberUuid(groupPath, username);
        } else {
            List<WsHasMemberResult> hasMemberResults =
                    Arrays.asList(grouperApiService.hasMemberResults(groupPath, username).getResults());
            for (WsHasMemberResult hasMemberResult : hasMemberResults) {
                if (hasMemberResult.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Returns true if the person is a member of the group
    @Override
    public boolean isMember(String groupPath, Person person) {
        if (person.getUsername() != null) {
            return isMember(groupPath, person.getUsername());
        }

        List<WsHasMemberResult> hasMemberResults =
                Arrays.asList(grouperApiService.hasMemberResults(groupPath, person).getResults());

        for (WsHasMemberResult hasMemberResult : hasMemberResults) {
            if (hasMemberResult.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                return true;
            }
        }
        return false;

    }

    // Returns true if the person is a member of the group
    public boolean isMemberUuid(String groupPath, String idnum) {
        logger.info("isMember; groupPath: " + groupPath + "; uuid: " + idnum + ";");

        List<WsHasMemberResult> hasMemberResults =
                Arrays.asList(grouperApiService.hasMemberResults(groupPath, idnum).getResults());

        for (WsHasMemberResult hasMemberResult : hasMemberResults) {
            if (hasMemberResult.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                return true;
            }
        }
        return false;
    }

    // Returns true if the user is in the owner group of the grouping
    @Override
    public boolean isOwner(String groupingPath, String username) {
        return isMember(groupingPath + OWNERS, username);
    }

    @Override
    public boolean isOwner(String username) {
        return isMember(OWNERS_GROUP, username);
    }

    // Returns true if the user is in the admins group
    @Override
    public boolean isAdmin(String username) {
        return isMember(GROUPING_ADMINS, username);
    }

    // Returns true if the user is in the apps group
    @Override
    public boolean isApp(String username) {
        return isMember(GROUPING_APPS, username);
    }

    /**
     * Get a mapping of all user attributes (uid, composite name, last name, first name, uhUuid) pertaining to the uid
     * or uhUuid passed through userIdentifier. Passing an invalid userIdentifier or current user will return a mapping
     * with null values.
     */
    public Person getMemberAttributes(String currentUser, String userIdentifier) {
        if (!isAdmin(currentUser) && !isOwner(currentUser)) {
            return new Person(helperService.memberAttributeMapSetKeys());
        }
        Person person = new Person();
        int numberOfAttributes = 5;
        WsSubjectLookup lookup = grouperApiService.subjectLookup(userIdentifier);
        WsGetSubjectsResults results = grouperApiService.subjectsResults(lookup);

        if (SUBJECT_NOT_FOUND.equals(results.getWsSubjects()[0].getResultCode())) {
            throw new GcWebServiceError(SUBJECT_NOT_FOUND);
        }
        for (int i = 0; i < numberOfAttributes; i++) {
            person.getAttributes()
                    .put(results.getSubjectAttributeNames()[i], results.getWsSubjects()[0].getAttributeValues()[i]);
        }
        return person;
    }

    /**
     * Get a list of GroupPaths the user owns.
     */
    @Override
    public List<GroupingPath> getOwnedGroupings(String currentUser, String user) {
        List<String> pathStrings = groupingAssignmentService.getGroupPaths(currentUser, user);
        List<GroupingPath> groupingPaths = new ArrayList<>();

        for (String path : pathStrings) {
            if (path.endsWith(OWNERS)) {
                groupingPaths.add(new GroupingPath(helperService.parentGroupingPath(path)));
            }
        }
        return groupingPaths;
    }

    /**
     * Get the number of groupings a user owns.
     */
    @Override
    public Integer getNumberOfGroupings(String currentUser, String uid) {
        return getOwnedGroupings(currentUser, uid).size();
    }
}