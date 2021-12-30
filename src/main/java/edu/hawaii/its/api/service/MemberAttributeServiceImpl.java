package edu.hawaii.its.api.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("memberAttributeService")
public class MemberAttributeServiceImpl implements MemberAttributeService {

    @Value("${groupings.api.privilege_opt_out}")
    private String PRIVILEGE_OPT_OUT;

    @Value("${groupings.api.privilege_opt_in}")
    private String PRIVILEGE_OPT_IN;

    @Value("${groupings.api.success_allowed}")
    private String SUCCESS_ALLOWED;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_apps}")
    private String GROUPING_APPS;

    @Value("${groupings.api.self_opted}")
    private String SELF_OPTED;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    @Value("${groupings.api.assign_type_immediate_membership}")
    private String ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP;

    @Value("${groupings.api.is_member}")
    private String IS_MEMBER;

    @Value("${groupings.api.person_attributes.uhuuid}")
    private String UHUUID;

    @Value("${groupings.api.person_attributes.username}")
    private String UID;

    @Value("${groupings.api.person_attributes.first_name}")
    private String FIRST_NAME;

    @Value("${groupings.api.person_attributes.last_name}")
    private String LAST_NAME;

    @Value("${groupings.api.person_attributes.composite_name}")
    private String COMPOSITE_NAME;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    private GrouperFactoryService grouperFS;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    private HelperService helperService;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    public static final Log logger = LogFactory.getLog(MemberAttributeServiceImpl.class);

    @Override
    public boolean isOwner(String username) {
        return isMember(OWNERS_GROUP, username);
    }

    // Return true if the membership between the group and user has the self-opted attribute, false otherwise
    @Override
    public boolean isSelfOpted(String groupPath, String username) {
        logger.info("isSelfOpted; group: " + groupPath + "; username: " + username + ";");

        if (isMember(groupPath, username)) {
            WsGetMembershipsResults wsGetMembershipsResults = helperService.membershipsResults(username, groupPath);
            String membershipID = helperService.extractFirstMembershipID(wsGetMembershipsResults);

            WsAttributeAssign[] wsAttributes =
                    getMembershipAttributes(ASSIGN_TYPE_IMMEDIATE_MEMBERSHIP, SELF_OPTED, membershipID);

            for (WsAttributeAssign att : wsAttributes) {
                if (att.getAttributeDefNameName() != null) {
                    if (att.getAttributeDefNameName().equals(SELF_OPTED)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //checks to see if the user has the privilege in that group
    public WsGetGrouperPrivilegesLiteResult getGrouperPrivilege(String username, String privilegeName,
            String groupPath) {
        logger.info("getGrouperPrivilege; username: "
                + username
                + "; group: "
                + groupPath
                + "; privilegeName: "
                + privilegeName
                + ";");

        WsSubjectLookup lookup = grouperFS.makeWsSubjectLookup(username);

        return grouperFS.makeWsGetGrouperPrivilegesLiteResult(groupPath, privilegeName, lookup);
    }

    //returns true if the group allows that user to opt in
    @Override
    public boolean isGroupCanOptIn(String optInUsername, String groupPath) {
        logger.info("groupOptInPermission; group: " + groupPath + "; username: " + optInUsername + ";");

        WsGetGrouperPrivilegesLiteResult result = getGrouperPrivilege(optInUsername, PRIVILEGE_OPT_IN, groupPath);

        return result
                .getResultMetadata()
                .getResultCode()
                .equals(SUCCESS_ALLOWED);
    }

    //returns true if the group allows that user to opt out
    @Override
    public boolean isGroupCanOptOut(String optOutUsername, String groupPath) {
        logger.info("groupOptOutPermission; group: " + groupPath + "; username: " + optOutUsername + ";");
        WsGetGrouperPrivilegesLiteResult result = getGrouperPrivilege(optOutUsername, PRIVILEGE_OPT_OUT, groupPath);

        return result
                .getResultMetadata()
                .getResultCode()
                .equals(SUCCESS_ALLOWED);
    }

    // Returns true if the user is a member of the group via username or UH id
    @Override
    public boolean isMember(String groupPath, String username) {
        logger.info("isMember; groupPath: " + groupPath + "; username: " + username + ";");

        if (isUhUuid(username)) {
            return isMemberUuid(groupPath, username);
        } else {
            WsHasMemberResults memberResults = grouperFS.makeWsHasMemberResults(groupPath, username);

            WsHasMemberResult[] memberResultArray = memberResults.getResults();

            for (WsHasMemberResult hasMember : memberResultArray) {
                if (hasMember.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
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

        WsHasMemberResults memberResults = grouperFS.makeWsHasMemberResults(groupPath, person);

        WsHasMemberResult[] memberResultArray = memberResults.getResults();

        for (WsHasMemberResult hasMember : memberResultArray) {
            if (hasMember.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                return true;
            }
        }
        return false;

    }

    // Returns true if the person is a member of the group
    public boolean isMemberUuid(String groupPath, String idnum) {
        logger.info("isMember; groupPath: " + groupPath + "; uuid: " + idnum + ";");

        WsHasMemberResults memberResults = grouperFS.makeWsHasMemberResults(groupPath, idnum);

        WsHasMemberResult[] memberResultArray = memberResults.getResults();

        for (WsHasMemberResult hasMember : memberResultArray) {
            if (hasMember.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
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

    @Override
    // returns true if username is a UH id number
    public boolean isUhUuid(String naming) { return naming != null && naming.matches("\\d+"); }

    // Checks to see if a membership has an attribute of a specific type and returns the list if it does
    public WsAttributeAssign[] getMembershipAttributes(String assignType, String attributeUuid, String membershipID) {
        logger.info("getMembershipAttributes; assignType: "
                + assignType
                + "; name: "
                + attributeUuid
                + "; membershipID: "
                + membershipID
                + ";");

        WsGetAttributeAssignmentsResults attributeAssignmentsResults =
                grouperFS.makeWsGetAttributeAssignmentsResultsForMembership(
                        assignType,
                        attributeUuid,
                        membershipID);

        WsAttributeAssign[] wsAttributes = attributeAssignmentsResults.getWsAttributeAssigns();

        return wsAttributes != null ? wsAttributes : grouperFS.makeEmptyWsAttributeAssignArray();
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
        WsSubjectLookup lookup;
        WsGetSubjectsResults results;
        int numberOfAttributes = 5;
        try {
            lookup = grouperFS.makeWsSubjectLookup(userIdentifier);
            results = grouperFS.makeWsGetSubjectsResults(lookup);
            for (int i = 0; i < numberOfAttributes; i++) {
                person.getAttributes()
                        .put(results.getSubjectAttributeNames()[i], results.getWsSubjects()[0].getAttributeValues()[i]);
            }
        } catch (NullPointerException npe) {
            person.setAttributes(helperService.memberAttributeMapSetKeys());
        }
        return person;
    }

    // Returns a specific user's attribute (FirstName, LastName, etc.) based on the username
    // Not testable with Unit test as needs to connect to Grouper database to work, not mock db
    public String getSpecificUserAttribute(String ownerUsername, String username, int attribute)
            throws GcWebServiceError {
        WsSubject[] subjects;
        WsSubjectLookup lookup;
        String[] attributeValues = new String[5];
        Map<String, String> mapping = new HashMap<>();

        if (isAdmin(ownerUsername) || groupingAssignmentService.groupingsOwned(
                groupingAssignmentService.getGroupPaths(ownerUsername, ownerUsername)).size() != 0) {
            try {
                lookup = grouperFS.makeWsSubjectLookup(username);
                WsGetSubjectsResults results = grouperFS.makeWsGetSubjectsResults(lookup);
                subjects = results.getWsSubjects();

                attributeValues = subjects[0].getAttributeValues();
                return attributeValues[attribute];

            } catch (NullPointerException npe) {
                throw new GcWebServiceError("Error 404 Not Found");
            }
        } else {
            return attributeValues[attribute];
        }

    }

    @Override
    public List<Person> searchMembers(String groupPath, String username) {

        List<Person> members = new ArrayList<>();

        WsHasMemberResults results = grouperFS.makeWsHasMemberResults(groupPath, username);
        WsHasMemberResult[] memberResultArray = results.getResults();

        for (WsHasMemberResult hasMember : memberResultArray) {

            if (hasMember.getResultMetadata().getResultCode().equals(IS_MEMBER)) {
                String memberName = hasMember.getWsSubject().getName();
                String memberUuid = hasMember.getWsSubject().getId();
                String memberUsername = hasMember.getWsSubject().getIdentifierLookup();

                members.add(new Person(memberName, memberUuid, memberUsername));
            }
        }
        return members;
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
     * Get a list of memberships pertaining to uid.
     */
    @Override public List<Membership> getMembershipResults(String owner, String uid) {
        String action = "getMembershipResults; owner: " + owner + "; uid: " + uid + ";";
        logger.info(action);

        if (!isAdmin(owner) && !owner.equals(uid)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        List<Membership> memberships = new ArrayList<>();
        List<String> groupPaths;
        List<String> optOutList;
        try {
            groupPaths = groupingAssignmentService.getGroupPaths(owner, uid);
            optOutList = groupingAssignmentService.getOptOutGroups(owner, uid);
        } catch (GcWebServiceError e) {
            return memberships;
        }
        Map<String, List<String>> pathMap = new HashMap<>();
        for (String pathToCheck : groupPaths) {
            if (!pathToCheck.endsWith(INCLUDE) && !pathToCheck.endsWith(EXCLUDE) && !pathToCheck.endsWith(BASIS)
                    && !pathToCheck.endsWith(OWNERS)) {
                continue;
            }
            String parentPath = helperService.parentGroupingPath(pathToCheck);
            if (!pathMap.containsKey(parentPath)) {
                pathMap.put(parentPath, new ArrayList<>());
            }
            pathMap.get(parentPath).add(pathToCheck);
        }

        for (Map.Entry<String, List<String>> entry : pathMap.entrySet()) {
            String groupingPath = entry.getKey();
            List<String> paths = entry.getValue();
            Membership membership = new Membership();
            for (String path : paths) {
                if (path.endsWith(BASIS)) {
                    membership.setInBasis(true);
                }
                if (path.endsWith(INCLUDE)) {
                    membership.setInInclude(true);
                }
                if (path.endsWith(EXCLUDE)) {
                    membership.setInExclude(true);
                }
                if (path.endsWith(OWNERS)) {
                    membership.setInOwner(true);
                }
            }
            membership.setPath(groupingPath);
            membership.setOptOutEnabled(optOutList.contains(groupingPath));
            membership.setName(helperService.nameGroupingPath(groupingPath));
            if (!membership.isInExclude() && !membership.isInBasis()) {
                memberships.add(membership);
            }
        }
        return memberships;
    }

    /**
     * Get's the number of groupings a user owns.
     */
    @Override
    public Integer getNumberOfGroupings(String currentUser, String uid) {
        return getOwnedGroupings(currentUser, uid).size();
    }

    // Get the number of memberships the current user has
    @Override public Integer getNumberOfMemberships(String currentUser, String uid) {
        return getMembershipResults(currentUser, uid).size();
    }
}