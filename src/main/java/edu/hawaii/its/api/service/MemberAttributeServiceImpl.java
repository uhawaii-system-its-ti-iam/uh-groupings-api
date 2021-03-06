package edu.hawaii.its.api.service;

import edu.hawaii.its.api.repository.PersonRepository;
import edu.hawaii.its.api.type.GroupingPath;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.Person;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembershipsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private HelperService hs;

    @Autowired
    private GroupingAssignmentService groupingAssignmentService;

    public static final Log logger = LogFactory.getLog(MemberAttributeServiceImpl.class);

    @Override
    public boolean isOwner(String username) {
        return isMember(OWNERS_GROUP, username);
    }

    //return true if the membership between the group and user has the self-opted attribute, false otherwise
    @Override
    public boolean isSelfOpted(String groupPath, String username) {
        logger.info("isSelfOpted; group: " + groupPath + "; username: " + username + ";");

        if (isMember(groupPath, username)) {
            WsGetMembershipsResults wsGetMembershipsResults = hs.membershipsResults(username, groupPath);
            String membershipID = hs.extractFirstMembershipID(wsGetMembershipsResults);

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

    //give ownership to a new user
    @Override
    public GroupingsServiceResult assignOwnership(String groupingPath, String ownerUsername, String newOwnerUsername) {
        logger.info("assignOwnership; groupingPath: "
                + groupingPath
                + "; ownerUsername: "
                + ownerUsername
                + "; newOwnerUsername: "
                + newOwnerUsername
                + ";");
        String action;
        GroupingsServiceResult ownershipResult;

        if (isUhUuid(newOwnerUsername)) {
            action = "give user with id " + newOwnerUsername + " ownership of " + groupingPath;
        } else {
            action = "give " + newOwnerUsername + " ownership of " + groupingPath;
        }

        if (!isOwner(groupingPath, ownerUsername) && !isAdmin(ownerUsername)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        WsSubjectLookup user = grouperFS.makeWsSubjectLookup(ownerUsername);
        WsAddMemberResults amr = grouperFS.makeWsAddMemberResults(groupingPath + OWNERS, user, newOwnerUsername);

        ownershipResult = hs.makeGroupingsServiceResult(amr, action);

        membershipService.updateLastModified(groupingPath);
        membershipService.updateLastModified(groupingPath + OWNERS);

        return ownershipResult;
    }

    //remove ownership of a grouping from a current owner
    @Override
    public GroupingsServiceResult removeOwnership(String groupingPath, String actor, String ownerToRemove) {
        logger.info("removeOwnership; grouping: "
                + groupingPath
                + "; username: "
                + actor
                + "; ownerToRemove: "
                + ownerToRemove
                + ";");

        GroupingsServiceResult ownershipResults;
        String action = "remove ownership of " + groupingPath + " from " + ownerToRemove;

        if (!isOwner(groupingPath, actor) && !isAdmin(actor)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }

        WsSubjectLookup lookup = grouperFS.makeWsSubjectLookup(actor);
        WsDeleteMemberResults memberResults = grouperFS.makeWsDeleteMemberResults(
                groupingPath + OWNERS,
                lookup,
                ownerToRemove);
        ownershipResults = hs.makeGroupingsServiceResult(memberResults, action);

        membershipService.updateLastModified(groupingPath);
        membershipService.updateLastModified(groupingPath + OWNERS);

        return ownershipResults;
    }

    //returns true if the user is a member of the group via username or UH id
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

    //returns true if the person is a member of the group
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

    // returns true if the person is a member of the group
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

    //returns true if the user is in the owner group of the grouping
    @Override
    public boolean isOwner(String groupingPath, String username) {
        return isMember(groupingPath + OWNERS, username);
    }

    //returns true if the user is in the admins group
    @Override
    public boolean isAdmin(String username) {
        return isMember(GROUPING_ADMINS, username);
    }

    //returns true if the user is in the apps group
    @Override
    public boolean isApp(String username) {
        return isMember(GROUPING_APPS, username);
    }

    // returns true if username is a UH id number
    @Override
    public boolean isUhUuid(String naming) {
        return naming.matches("\\d+");
    }

    //checks to see if a membership has an attribute of a specific type and returns the list if it does
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

    /*
     * Covered by Integration Tests
     *
     * Returns a user's attributes (FirstName(givenName), LastName(sn), Composite Name(cn), Username(uid), UH User ID(uhUuid)) based on the username.
     * If the requester of the information is not a superuser or owner, then the function returns a mapping with empty values.
     *
     * Not testable with Unit test as needs to connect to Grouper database to work, not mock db.
     *
     */
    public Map<String, String> getUserAttributes(String ownerUsername, String username) throws GcWebServiceError {
        WsSubjectLookup lookup;
        WsGetSubjectsResults results;
        String[] attributeValues = new String[5];
        Map<String, String> mapping = new HashMap<>();

        // Checks to make sure the user requesting information of another is a superuser or the owner of the group.
        if (isAdmin(ownerUsername) || groupingAssignmentService.groupingsOwned(
                groupingAssignmentService.getGroupPaths(ownerUsername, ownerUsername)).size() != 0) {
            try {

                // Makes a call to GrouperClient and creates a WebService Subject Lookup of specified user.
                lookup = grouperFS.makeWsSubjectLookup(username);

                /*
                 * Using the WebService Subject Lookup it gets the gets the WebService Subject Results.
                 * The results returns information about the user including the user's attributes.
                 * In the results are the attributes and attribute names.
                 */
                results = grouperFS.makeWsGetSubjectsResults(lookup);

                // Maps the attribute to the attribute name.
                for (int i = 0; i < attributeValues.length; i++) {
                    mapping.put(results.getSubjectAttributeNames()[i],
                            results.getWsSubjects()[0].getAttributeValues()[i]);
                }
                return mapping;

            } catch (NullPointerException npe) {
                throw new GcWebServiceError("Error 404 Not Found");
            }
        } else {
            String[] subjectAttributeNames = { UID, COMPOSITE_NAME, LAST_NAME, FIRST_NAME, UHUUID };
            for (int i = 0; i < attributeValues.length; i++) {
                mapping.put(subjectAttributeNames[i], "");
            }
            return mapping;
        }

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
     * Return true if usernameInQuestion is an owner, otherwise return false.
     */
    @Override
    public Boolean getIsOwner(String currentUser, String usernameInQuestion) {
        logger.info("getIsOwner: " + "currentUser: " + currentUser + ";, " + "usernameInQuestion: " + usernameInQuestion
                + ";");

        if (!isOwner(currentUser)) {
            throw new AccessDeniedException(INSUFFICIENT_PRIVILEGES);
        }
        try {
            return groupingAssignmentService
                    .groupingsOwned(groupingAssignmentService.getGroupPaths(currentUser, usernameInQuestion)).size()
                    > 0;
        } catch (AccessDeniedException | GcWebServiceError e) {
            return false;
        }
    }

    /**
     * Return true if usernameInQuestion is an admin, otherwise return false.
     */
    @Override
    public Boolean getIsAdmin(String currentUser, String usernameInQuestion) {
        logger.info("getIsAdmin: " + "currentUser: " + currentUser + ";, " + "usernameInQuestion: " + usernameInQuestion
                + ";");
        try {
            return groupingAssignmentService.adminLists(usernameInQuestion).getAdminGroup().getMembers().size() > 0;
        } catch (AccessDeniedException | GcWebServiceError e) {
            return false;
        }

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
                groupingPaths.add(new GroupingPath(hs.parentGroupingPath(path)));
            }
        }
        return groupingPaths;
    }
}
