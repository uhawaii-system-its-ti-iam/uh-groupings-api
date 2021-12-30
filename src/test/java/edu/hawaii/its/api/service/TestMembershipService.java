package edu.hawaii.its.api.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.AddMemberResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;
import edu.hawaii.its.api.type.Membership;
import edu.hawaii.its.api.type.RemoveMemberResult;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class TestMembershipService {

    @Value("${groupings.api.test.grouping_many}")
    private String GROUPING;
    @Value("${groupings.api.test.grouping_many_basis}")
    private String GROUPING_BASIS;
    @Value("${groupings.api.test.grouping_many_include}")
    private String GROUPING_INCLUDE;
    @Value("${groupings.api.test.grouping_many_exclude}")
    private String GROUPING_EXCLUDE;
    @Value("${groupings.api.test.grouping_many_owners}")
    private String GROUPING_OWNERS;
    @Value("${groupings.api.test.grouping_many_extra}")
    private String GROUPING_EXTRA;

    @Value("${groupings.api.basis}")
    private String BASIS;

    @Value("${groupings.api.exclude}")
    private String EXCLUDE;

    @Value("${groupings.api.include}")
    private String INCLUDE;

    @Value("${groupings.api.owners}")
    private String OWNERS;

    @Value("${groupings.api.test.admin_user}")
    private String ADMIN;

    @Value("${groupings.api.success}")
    private String SUCCESS;

    @Value("${groupings.api.yyyymmddThhmm}")
    private String YYYYMMDDTHHMM;

    @Value("${groupings.api.assign_type_group}")
    private String ASSIGN_TYPE_GROUP;

    @Value("${groupings.api.listserv}")
    private String LISTSERV;

    @Value("${groupings.api.releasedgrouping}")
    private String RELEASED_GROUPING;

    @Value("${groupings.api.test.usernames}")
    private String[] username;

    @Value("${groupings.api.failure}")
    private String FAILURE;

    @Value("${groupings.api.insufficient_privileges}")
    private String INSUFFICIENT_PRIVILEGES;

    @Autowired
    GroupAttributeService groupAttributeService;

    @Autowired
    GroupingAssignmentService groupingAssignmentService;

    @Autowired
    private MemberAttributeService memberAttributeService;

    @Autowired
    private MembershipService membershipService;

    @Autowired
    public Environment env; // Just for the settings check.

    @Autowired
    private GrouperFactoryService grouperFactoryService;

    @PostConstruct
    public void init() {
        Assert.hasLength(env.getProperty("grouperClient.webService.url"),
                "property 'grouperClient.webService.url' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.login"),
                "property 'grouperClient.webService.login' is required");
        Assert.hasLength(env.getProperty("grouperClient.webService.password"),
                "property 'grouperClient.webService.password' is required");
    }

    @Before
    public void setUp() throws IOException, MessagingException {
        //add ownership

        membershipService.addOwners(GROUPING, ADMIN, Collections.singletonList(username[0]));

        groupAttributeService.changeGroupAttributeStatus(GROUPING, username[0], LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, username[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, username[0], true);

        //Add to include.
        List<String> includeNames = new ArrayList<>();
        includeNames.add(username[0]);
        includeNames.add(username[1]);
        includeNames.add(username[2]);

        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, includeNames);

        // Add to basis (you cannot do this directly, so we add the user to one of the groups that makes up the basis).
        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ADMIN);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, username[3]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, username[4]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, username[5]);

        //Remove from exclude.
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, Collections.singletonList(username[4]));
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, Collections.singletonList(username[5]));

        //Add to exclude.
        membershipService.addGroupMembers(username[0], GROUPING_INCLUDE, Collections.singletonList(username[3]));

        //Add to basis.
        //membershipService.addGroupMember(username[0], GROUPING_BASIS, username[5]);

        //Remove ownership.
        membershipService.removeOwnerships(GROUPING, username[0], Arrays.asList(username));
    }

    @Test
    public void groupOptInPermissionTest() {
        assertTrue(memberAttributeService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(memberAttributeService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void getMembershipResultsTest() {
        List<Membership> memberships = memberAttributeService.getMembershipResults(username[0], username[0]);
        assertNotNull(memberships);
        assertTrue(memberships.size() != 0);
        Set<String> pathMap = new HashSet<>();
        for (Membership membership : memberships) {
            assertNotNull(membership.getPath());
            assertNotNull(membership.getName());
            // The membership's path should be a parent path.
            assertFalse(membership.getPath().endsWith(INCLUDE));
            assertFalse(membership.getPath().endsWith(EXCLUDE));
            assertFalse(membership.getPath().endsWith(BASIS));
            assertFalse(membership.getPath().endsWith(OWNERS));
            // The member should be in at least one of these.
            assertTrue(membership.isInBasis() || membership.isInExclude() || membership.isInInclude()
                    || membership.isInOwner());
            // Check for duplicate paths.
            assertTrue(pathMap.add(membership.getPath()));
        }
    }

    @Test
    public void groupOptOutPermissionTest() {
        assertTrue(memberAttributeService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(memberAttributeService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void updateLastModifiedTest() {
        // Test is accurate to the minute, and if checks to see if the current
        // time gets added to the lastModified attribute of a group if the
        // minute happens to change in between getting the time and setting
        // the time, the test will fail.

        final String group = GROUPING_INCLUDE;

        GroupingsServiceResult gsr = membershipService.updateLastModified(group);
        String dateStr = gsr.getAction().split(" to time ")[1];

        WsGetAttributeAssignmentsResults assignments =
                groupAttributeService.attributeAssignmentsResults(ASSIGN_TYPE_GROUP, group, YYYYMMDDTHHMM);
        String assignedValue = assignments.getWsAttributeAssigns()[0].getWsAttributeAssignValues()[0].getValueSystem();

        assertThat(assignedValue, is(dateStr));
    }

    @Test
    public void addRemoveSelfOptedTest() {

        //username[2] is not in the include, but not self opted.
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //Add the self opted attribute for username[2]'s membership for the include group.
        membershipService.addSelfOpted(GROUPING_INCLUDE, username[2]);

        //username[2] should now be self opted.
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //remove the self opted attribute for username[2]'s membership from the include group.
        membershipService.removeSelfOpted(GROUPING_INCLUDE, username[2]);

        //username[2] should no longer be self opted into the include.
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, username[2]));

        //Try to add self opted attribute when not in the group.
        GroupingsServiceResult groupingsServiceResult;

        try {
            groupingsServiceResult = membershipService.addSelfOpted(GROUPING_EXCLUDE, username[2]);
        } catch (GroupingsServiceResultException gsre) {
            groupingsServiceResult = gsre.getGsr();
        }
        assertTrue(groupingsServiceResult.getResultCode().startsWith(FAILURE));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[2]));
    }

    @Test
    public void groupOptPermissionTest() {
        assertTrue(memberAttributeService.isGroupCanOptOut(username[0], GROUPING_INCLUDE));
        assertTrue(memberAttributeService.isGroupCanOptOut(username[0], GROUPING_EXCLUDE));

        assertTrue(memberAttributeService.isGroupCanOptIn(username[0], GROUPING_INCLUDE));
        assertTrue(memberAttributeService.isGroupCanOptIn(username[0], GROUPING_EXCLUDE));
    }

    @Test
    public void listGroupsTest() {
        //todo
    }

    @Test
    public void getMembersTest() {
        String[] groupings = { GROUPING };
        Group group = groupingAssignmentService.getMembers(username[0], Arrays.asList(groupings)).get(GROUPING);
        List<String> usernames = group.getUsernames();

        assertTrue(usernames.contains(username[0]));
        assertTrue(usernames.contains(username[1]));
        assertTrue(usernames.contains(username[2]));
        assertTrue(usernames.contains(username[4]));
        assertTrue(usernames.contains(username[5]));
    }

    @Test
    public void addGroupingMembersTest() {
        String ownerUsername = username[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to include.
        List<String> validUsernames = new ArrayList<>(Arrays.asList(username).subList(0, 6));
        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_INCLUDE, validUsernames);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getUid());
            assertNotNull(addMemberResult.getUhUuid());
            assertNotNull(addMemberResult.getName());
        }

        // Add invalid users to include.
        List<String> invalidUsernames = new ArrayList<>();
        invalidUsernames.add("zz_zzz");
        invalidUsernames.add("ffff");
        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_INCLUDE, invalidUsernames);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertNull(addMemberResult.getUhUuid());
        }

        // Add valid users to exclude.
        validUsernames = new ArrayList<>(Arrays.asList(username).subList(0, 6));
        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_EXCLUDE, validUsernames);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getUid());
            assertNotNull(addMemberResult.getUhUuid());
            assertNotNull(addMemberResult.getName());
        }

        // Add invalid users to include.
        List<String> invalidUsernamesForExclude = new ArrayList<>();
        invalidUsernamesForExclude.add("zz_zzz");
        invalidUsernamesForExclude.add("ffff");
        addMemberResults =
                membershipService.addGroupMembers(ownerUsername, GROUPING_EXCLUDE, invalidUsernamesForExclude);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertNull(addMemberResult.getUhUuid());
        }

        // A non-owner attempts to add members.
        try {
            membershipService.addGroupMembers("zz_zz", GROUPING_INCLUDE, validUsernames);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        List<String> invalidUsers = new ArrayList<>();
        invalidUsers.add("zz_zzzzz");
        invalidUsers.add("aaaaaaa");

        addMemberResults = membershipService.addGroupMembers(ownerUsername, GROUPING_INCLUDE, invalidUsers);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertFalse(addMemberResult.isUserWasRemoved());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertEquals(FAILURE, addMemberResult.getResult());
        }

        // A group path ending in anything other than include or exclude should 404.
        try {
            membershipService.addGroupMembers(ownerUsername, GROUPING_OWNERS, validUsernames);
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }
    }

    @Test
    public void addIncludeMembersTest() {
        String ownerUsername = username[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to include.
        List<String> validUsernames = new ArrayList<>(Arrays.asList(username).subList(0, 6));
        addMemberResults = membershipService.addIncludeMembers(ownerUsername, GROUPING, validUsernames);
        Iterator<String> iter = validUsernames.iterator();
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getName());
            assertNotNull(addMemberResult.getUhUuid());
            assertEquals(iter.next(), addMemberResult.getUid());
        }
    }

    @Test
    public void addExcludeMembersTest() {
        String ownerUsername = username[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to exclude.
        List<String> validUsernames = new ArrayList<>(Arrays.asList(username).subList(0, 6));
        addMemberResults = membershipService.addExcludeMembers(ownerUsername, GROUPING, validUsernames);
        Iterator<String> iter = validUsernames.iterator();
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getName());
            assertNotNull(addMemberResult.getUhUuid());
            assertEquals(iter.next(), addMemberResult.getUid());
        }
    }

    @Test
    public void removeGroupingMembersTest() {

        String ownerUsername = username[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableUsernames = new ArrayList<>(Collections.singletonList(username[0]));

        // Remove a single member.
        removeMemberResults =
                membershipService.removeGroupMembers(ownerUsername, GROUPING_INCLUDE, removableUsernames);

        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertNotNull(removeMemberResult.getUhUuid());
            assertNotNull(removeMemberResult.getName());
            assertNotNull(removeMemberResult.getUid());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
        }

        // Remove multiple members.
        removableUsernames = new ArrayList<>(Arrays.asList(username).subList(1, 6));
        removeMemberResults =
                membershipService.removeGroupMembers(ownerUsername, GROUPING_INCLUDE, removableUsernames);
        Iterator<String> removableUsernamesIter = removableUsernames.iterator();
        Iterator<RemoveMemberResult> removedMemberResultsIter = removeMemberResults.iterator();

        while (removableUsernamesIter.hasNext() && removedMemberResultsIter.hasNext()) {
            RemoveMemberResult result = removedMemberResultsIter.next();
            String uid = removableUsernamesIter.next();
            assertTrue(result.isUserWasRemoved());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(uid, result.getUid());
            assertEquals(uid, result.getUhUuid());
            assertNotNull(result.getUhUuid());
            assertNotNull(result.getUid());
            assertNotNull(result.getName());
            assertEquals(GROUPING_INCLUDE, result.getPathOfRemoved());
        }

        // Try to remove non-members, the list of removableUsernames has already been removed above, thus attempting to
        // remove them again should fail.
        removeMemberResults =
                membershipService.removeGroupMembers(ownerUsername, GROUPING_INCLUDE, removableUsernames);
        removedMemberResultsIter = removeMemberResults.iterator();

        while (removedMemberResultsIter.hasNext()) {
            RemoveMemberResult result = removedMemberResultsIter.next();
            assertFalse(result.isUserWasRemoved());
            assertEquals(FAILURE, result.getResult());
        }

        List<String> invalidUsers = new ArrayList<>();
        invalidUsers.add("zzz_zz_zz");
        invalidUsers.add("aaa_aaaa");

        removeMemberResults = membershipService.removeGroupMembers(ownerUsername, GROUPING_INCLUDE, invalidUsers);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertFalse(removeMemberResult.isUserWasRemoved());
            assertNull(removeMemberResult.getName());
            assertNull(removeMemberResult.getUid());
            assertEquals(FAILURE, removeMemberResult.getResult());
        }

        try {
            membershipService.removeGroupMembers(ownerUsername, GROUPING_OWNERS, removableUsernames);
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }

    }

    @Test
    public void removeIncludeMembersTest() {
        String ownerUsername = username[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableUsernames = new ArrayList<>(Collections.singletonList(username[0]));
        removeMemberResults =
                membershipService.removeIncludeMembers(ownerUsername, GROUPING, removableUsernames);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
        }
    }

    @Test
    public void removeExcludeMembersTest() {
        String ownerUsername = username[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableUsernames = new ArrayList<>(Collections.singletonList(username[0]));
        removeMemberResults =
                membershipService.removeExcludeMembers(ownerUsername, GROUPING, removableUsernames);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(GROUPING_EXCLUDE, removeMemberResult.getPathOfRemoved());
        }
    }

    @Test
    public void optInTest() {
        String ownerUsername = username[0];
        List<AddMemberResult> optResults;

        optResults = membershipService.optIn(ownerUsername, GROUPING, ownerUsername);
        for (AddMemberResult optResult : optResults) {
            assertEquals(GROUPING_INCLUDE, optResult.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, optResult.getPathOfRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        }
    }

    @Test
    public void optOutTest() {
        String ownerUsername = username[0];
        List<AddMemberResult> optResults;

        optResults = membershipService.optOut(ownerUsername, GROUPING, ownerUsername);
        for (AddMemberResult optResult : optResults) {
            assertEquals(GROUPING_EXCLUDE, optResult.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, optResult.getPathOfRemoved());
            assertFalse(memberAttributeService.isMember(GROUPING, ownerUsername));
        }
    }

    //Add admin and remove admin in one test
    @Test
    public void adminTest() {

        GroupingsServiceResult results;

        //checks to see that username[3] is NOT an admin
        results = membershipService.removeAdmin(ADMIN, username[3]);

        //makes username[3] an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to make an already admin an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //removes username[3] as an admin
        results = membershipService.removeAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to remove an person that is not an admin
        results = membershipService.removeAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to make username[4] an admin but fails due to username[3] not being an admin
        try {
            membershipService.addAdmin(username[3], username[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //tries to remove username[4] as an admin but fails due to username[3] not being an admin
        try {
            membershipService.removeAdmin(username[3], username[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }
}