package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Person;
import edu.hawaii.its.api.wrapper.SubjectCommand;
import edu.hawaii.its.api.wrapper.SubjectResult;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private GrouperApiService grouperApiService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    // Returns true if the user is a member of the group via username or UH id
    public boolean isMember(String groupPath, String uhIdentifier) {
        logger.info("isMember; groupPath: " + groupPath + "; uhIdentifier: " + uhIdentifier + ";");

        if (isUhUuid(uhIdentifier)) {
            return isMemberUuid(groupPath, uhIdentifier);
        }

        WsHasMemberResults whmrs = grouperApiService.hasMemberResults(groupPath, uhIdentifier);
        WsHasMemberResult[] whmr = whmrs.getResults();
        List<WsHasMemberResult> hasMemberResults = Arrays.asList(whmr);
        for (WsHasMemberResult hasMemberResult : hasMemberResults) {
            if (hasMemberResult.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                return true;
            }
        }

        return false;
    }

    // Returns true if the person is a member of the group
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
    public boolean isMemberUuid(String groupPath, String uhUuid) {
        logger.info("isMember; groupPath: " + groupPath + "; uuid: " + uhUuid + ";");

        List<WsHasMemberResult> hasMemberResults =
                Arrays.asList(grouperApiService.hasMemberResults(groupPath, uhUuid).getResults());

        for (WsHasMemberResult hasMemberResult : hasMemberResults) {
            if (hasMemberResult.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if username is a UH id number
     */
    public boolean isUhUuid(String uhIdentifier) {
        return uhIdentifier != null && uhIdentifier.matches("\\d+");
    }

    // Returns true if the user is in the owner group of the grouping
    public boolean isOwner(String groupingPath, String uhIdentifier) {
        return isMember(groupingPath + GroupType.OWNERS.value(), uhIdentifier);
    }

    public boolean isOwner(String uhIdentifier) {
        return isMember(OWNERS_GROUP, uhIdentifier);
    }

    // Returns true if the user is in the admins group
    public boolean isAdmin(String uhIdentifier) {
        return isMember(GROUPING_ADMINS, uhIdentifier);
    }

    // Returns true if the user is in the apps group
    public boolean isApp(String uhIdentifier) {
        return isMember(GROUPING_APPS, uhIdentifier);
    }

    /**
     * Get a mapping of all user attributes (uid, composite name, last name, first name, uhUuid) pertaining to the uid
     * or uhUuid passed through uhIdentifier. Passing an invalid uhIdentifier or current user will return a mapping
     * with null values.
     */
    public Person getMemberAttributes(String currentUser, String uhIdentifier) {
        if (!isAdmin(currentUser) && !isOwner(currentUser)) {
            return new Person();
        }

        SubjectResult results = new SubjectCommand(uhIdentifier).execute();

        if (results.getResultCode().equals(SUBJECT_NOT_FOUND)) {
            return new Person();
        }

        Person person = new Person();
        int numberOfAttributes = results.getSubjectAttributeNameCount();
        for (int i = 0; i < numberOfAttributes; i++) {
            String key = results.getSubjectAttributeName(i);
            String value = results.getAttributeValue(i);
            person.addAttribute(key, value);
        }

        return person;
    }

    /**
     * Get a list of GroupPaths the user owns, by username or uhUuid.
     */
    public List<GroupingPath> getOwnedGroupings(String currentUser, String uhIdentifier) {
        List<String> pathStrings = groupingAssignmentService.getGroupPaths(currentUser, uhIdentifier, pathHasOwner());

        List<GroupingPath> groupingPaths = new ArrayList<>();
        for (String path : pathStrings) {
            String parentGroupingPath = groupingAssignmentService.parentGroupingPath(path);
            groupingPaths.add(new GroupingPath(parentGroupingPath,
                    grouperApiService.descriptionOf(parentGroupingPath)));
        }

        return groupingPaths;
    }

    /**
     * Get the number of groupings a user owns, by username or uhUuid.
     */
    public Integer numberOfGroupings(String currentUser, String uhIdentifier) {
        return groupingAssignmentService.getGroupPaths(currentUser, uhIdentifier, pathHasOwner()).size();
    }
}
