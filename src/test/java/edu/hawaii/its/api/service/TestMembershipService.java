package edu.hawaii.its.api.service;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GenericServiceResult;
import edu.hawaii.its.api.type.Group;
import edu.hawaii.its.api.type.GroupingsServiceResult;
import edu.hawaii.its.api.type.GroupingsServiceResultException;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;

import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

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
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

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

    @Autowired
    private HelperService helperService;

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

        memberAttributeService.assignOwnership(GROUPING, ADMIN, username[0]);

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
        membershipService.addGroupingMember(username[0], GROUPING, username[4]);
        membershipService.addGroupingMember(username[0], GROUPING, username[5]);

        //Add to exclude.
        membershipService.addGroupMember(username[0], GROUPING_EXCLUDE, username[3]);

        //Add to basis.
        //membershipService.addGroupMember(username[0], GROUPING_BASIS, username[5]);

        //Remove ownership.
        memberAttributeService.removeOwnership(GROUPING, username[0], username[2]);
        memberAttributeService.removeOwnership(GROUPING, username[0], username[4]);

    }

    @Test
    public void groupOptInPermissionTest() {
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[1], GROUPING_EXCLUDE));
    }

    @Test
    public void groupOptOutPermissionTest() {
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[1], GROUPING_EXCLUDE));
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
    public void optTest() {
        //Reset group.
        membershipService.removeSelfOpted(GROUPING_EXCLUDE, username[3]);

        //tst[3] is not in the composite or include, but is in the basis and exclude.
        //tst[3] is not self opted into the exclude.
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));

        //Non super user tries to opt another user in.
        assertFalse(memberAttributeService.isSuperuser(username[0]));
        membershipService.optIn(username[0], GROUPING, username[3]);
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        membershipService.optOut(username[0], GROUPING, username[3]);
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));

        //tst[3] opts in to the Grouping.
        membershipService.optIn(username[3], GROUPING);
        //tst[3] should still be in the basis and now also in the Grouping.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        //tst[3] is no longer in the exclude, and because tst[3] is in the basis,
        //tst[3] does not get added to the include.
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //tst[3] opts out of the Grouping.
        membershipService.optOut(username[3], GROUPING);
        //tst[3] is still in basis, now in exclude and not in Grouping or include.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        //tst[3] is now self opted into exclude.
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));

        //Admins can opt other users.
        membershipService.optIn(ADMIN, GROUPING, username[3]);
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        membershipService.optOut(ADMIN, GROUPING, username[3]);
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, username[3]));
    }

    //Issue with not finding group on the server when calling is owner while getGroupPaths is able to find them.
    @Test
    public void listOwnedTest() {

        // Tests that when there is no groups owned, the list is empty.
        assertTrue(membershipService.listOwned(ADMIN, username[1]).isEmpty());

        // Adds user to owners of GROUPING 1.
        membershipService.addGroupMember(username[0], GROUPING_OWNERS, username[1]);

        // Tests that the list now contains the path to GROUPING 1 since user is now an owner.
        assertTrue(membershipService.listOwned(ADMIN, username[1]).get(0).equals(GROUPING));

        try {
            // Tests if a non admin can access users groups owned.
            membershipService.listOwned(username[0], username[1]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //Reset ownership.
        membershipService.deleteGroupMember(username[0], GROUPING_OWNERS, username[1]);
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
        assertTrue(membershipService.isGroupCanOptOut(username[0], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(username[0], GROUPING_EXCLUDE));

        assertTrue(membershipService.isGroupCanOptIn(username[0], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(username[0], GROUPING_EXCLUDE));
    }

    // Rebase, should test for code coverage purposes.
    // Related to ticket-500, used hardcoded values that were deleted.
    @Ignore
    @Test
    public void addMemberAsTest() {

        //username[3] is in the basis and exclude, not the composite or include.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //An owner adds username[3] to the include group.
        List<GroupingsServiceResult> addMember =
                membershipService.addGroupMember(username[0], GROUPING_INCLUDE, username[3]);

        //The addition was successful.
        assertTrue(addMember.get(0).getResultCode().startsWith(SUCCESS));
        //username[3] is in the basis, include and composite, not the exclude.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //Put username[3] back in the exclude group.
        addMember = membershipService.addGroupMember(username[0], GROUPING_EXCLUDE, username[3]);

        //The addition was successful.
        assertThat(SUCCESS, is(addMember.get(0).getResultCode()));
        //username[3] is in the basis and exclude, not the composite or include.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //Test adding when already in group.
        addMember = membershipService.addGroupMember(username[0], GROUPING_EXCLUDE, username[3]);
        //The addition was successful.
        assertTrue(addMember.get(0).getResultCode().startsWith(SUCCESS));
        //username[3] is in the basis and exclude, not the composite or include.
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
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
        assertFalse(usernames.contains(username[3]));
        assertTrue(usernames.contains(username[4]));
        assertTrue(usernames.contains(username[5]));
    }

    @Test
    public void addGroupingMemberByUuidTest() {

        List<GroupingsServiceResult> results;
        GroupingsServiceResult sResults;
        String ownerUsername = username[0];
        String uid = "";

        //username[1] is in the composite.
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //Add member already in the group.
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[1] is in the composite.
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //username[3] is not in the composite(in the exclude).
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //Add member not in the composite but in the basis.
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is now in the Composite via basis.
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //Removes username[1] from the group.
        membershipService.deleteGroupingMember(ownerUsername, GROUPING, username[1]);

        //Checks to see if username[1] is not in the basis or the composite.
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //Adds to group.
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to make sure user is in composite and include and nothing else.
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //Adds username[3] to the include and removes from the exclude.
        membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[3]);

        //username[3] is in the composite, basis and include.
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //Delete member from grouping.
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is in the composite, and basis.
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //Checks to see where username 4 is in the group.
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //Has a non owner try to remove the owner.
        try {
            membershipService.deleteGroupingMember(username[4], GROUPING, ownerUsername);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //Makes sure that owner is still in the group.
        assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, ownerUsername));

    }

    @Test
    public void addGroupingMemberTest() {
        List<GroupingsServiceResult> results;
        GroupingsServiceResult sResults;
        String ownerUsername = username[0];
        String uid = "";

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //add member already in the group
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[1] is in the composite
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //username[3] is not in the composite(in the exclude)
        assertFalse(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //add member not in the composite but in the basis
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is now in the Composite via basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //removes username[1] from the group
        membershipService.deleteGroupingMember(ownerUsername, GROUPING, username[1]);

        //Checks to see if username[1] is not in the basis or the composite
        assertFalse(memberAttributeService.isMember(GROUPING, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds to group
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[1]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //Checks to make sure user is in composite and include and nothing else
        assertTrue(memberAttributeService.isMember(GROUPING, username[1]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[1]));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[1]));

        //adds username[3] to the include and removes from the exclude
        membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[3]);

        //username[3] is in the composite, basis and include
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //delete member from grouping
        results = membershipService.addGroupingMember(ownerUsername, GROUPING, username[3]);
        assertTrue(results.get(0).getResultCode().startsWith(SUCCESS));

        //username[3] is in the composite, and basis
        assertTrue(memberAttributeService.isMember(GROUPING, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[3]));

        //checks to see where username 4 is in the group
        assertTrue(memberAttributeService.isMember(GROUPING, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[4]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[4]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[4]));

        //has a non owner try to remove the owner
        try {
            membershipService.deleteGroupingMember(username[4], GROUPING, ownerUsername);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //Makes sure that owner is still in the group
        assertTrue(memberAttributeService.isMember(GROUPING, ownerUsername));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, ownerUsername));
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, ownerUsername));
    }

    @Test
    @Ignore
    public void addGroupMemberTest() {
        List<GroupingsServiceResult> lResults;
        GroupingsServiceResult result;
        String ownerUsername = username[0];

        //Checks to see that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //tries to add a member to a group not allowed, i.e basis
        try {
            lResults = membershipService.addGroupMember(ownerUsername, GROUPING_BASIS, username[2]);
            assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));
        } catch (GroupingsServiceResultException e) {
            result = e.getGsr();
            assertTrue(result.getResultCode().startsWith(FAILURE));
        }

        //checks to make sure that user is NOT in basis
        assertFalse(memberAttributeService.isMember(GROUPING_BASIS, username[2]));

        //checks if user is an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMember(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks if user is a superuser
        assertFalse(memberAttributeService.isSuperuser(username[2]));

        //chceks to make sure user is not part of include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to see if a non superuser/owner can add a person
        try {
            lResults = membershipService.addGroupMember(username[2], GROUPING_INCLUDE, username[3]);
            assertTrue(lResults.get(0).getResultCode().startsWith(FAILURE));
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //checks to make sure user is not in include
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is in exclude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //adds user who was in exlcude to include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[3]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[3]));

        //checks to make sure user is NOT in exclude
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[3]));

        //checks to make sure user is in the group but not in exclude or include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //tries adding user to the include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[5]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks the user is in include
        assertTrue(memberAttributeService.isMember(GROUPING, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_BASIS, username[5]));
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[5]));
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, username[5]));

        //checks to make sure user is in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add a user already in a group
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_INCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in include
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //adds user to exlcude when user is in include
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure it is actually in the exclude and out of include
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));
        assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[2]));

        //tries to add user who is already in the exclude
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_EXCLUDE, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //checks to make sure user is still in exlcude
        assertTrue(memberAttributeService.isMember(GROUPING_EXCLUDE, username[2]));

        //checks to make user is not an owner
        assertFalse(memberAttributeService.isOwner(GROUPING, username[2]));

        //adds user to owner list
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        //tries to add user who is already owner
        lResults = membershipService.addGroupMember(ownerUsername, GROUPING_OWNERS, username[2]);
        assertTrue(lResults.get(0).getResultCode().startsWith(SUCCESS));

        //Checks that user is owner
        assertTrue(memberAttributeService.isOwner(GROUPING, username[2]));

        membershipService.deleteGroupMember(ownerUsername, GROUPING_OWNERS, username[2]);
    }

    @Test
    public void addGroupMembersTest() throws IOException, MessagingException {
        String ownerUsername = username[0];

        List<GroupingsServiceResult> results;
        List<String> usernames = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            usernames.add(username[i]);
        }

        results = membershipService.addGroupMembers(ownerUsername, GROUPING_INCLUDE, usernames);

        for (GroupingsServiceResult result : results) {
            assertTrue(result.getResultCode().startsWith(SUCCESS));
        }

        //        for (int i = 0; i < 6; i++) {
        //            membershipService.deleteGroupMember(ownerUsername, GROUPING_INCLUDE, username[i]);
        //        }
    }

    @Test
    public void removeGroupMembersTest() {
        // Remove from include
        List<String> includeNames = new ArrayList<>(Arrays.asList(username).subList(0, 6));
        GenericServiceResult genericServiceResult =
                membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, includeNames);

        assertEquals(SUCCESS, genericServiceResult.getGroupingsServiceResult().getResultCode());

        for (int i = 0; i < 3; i++) {
            assertFalse(memberAttributeService.isMember(GROUPING_INCLUDE, username[i]));
        }
        assertNotEquals(includeNames, genericServiceResult.get("membersDeleted"));
        for (int i = 3; i < 6; i++) {
            includeNames.remove(username[i]);
        }
        assertEquals(includeNames, genericServiceResult.get("membersDeleted"));

        // Remove from exclude and check singleton deletion.
        List<String> singletonRemoval = new ArrayList<>(Collections.singletonList(username[3]));
        GenericServiceResult singletonResult =
                membershipService.removeGroupMembers(ADMIN, GROUPING_EXCLUDE, singletonRemoval);

        // Check list
        assertFalse(memberAttributeService.isMember(GROUPING_EXCLUDE, singletonRemoval.get(0)));
        // Check Result Code
        assertEquals(SUCCESS, singletonResult.getGroupingsServiceResult().getResultCode());
        // Check result membersDeleted list
        assertEquals(singletonRemoval, singletonResult.get("membersDeleted"));

        // Check invalid list
        List<String> invalidList = new ArrayList<>(Arrays.asList("zzz", "a"));
        GenericServiceResult invalidResult = membershipService.removeGroupMembers(ADMIN, GROUPING_INCLUDE, invalidList);
        assertEquals(FAILURE, invalidResult.getGroupingsServiceResult().getResultCode());

    }

    //Add admin and delete admin in one test
    @Test
    public void adminTest() {

        GroupingsServiceResult results;

        //checks to see that username[3] is NOT an admin
        results = membershipService.deleteAdmin(ADMIN, username[3]);
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //makes username[3] an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to make sure that username[3] is an admin
        assertTrue(memberAttributeService.isSuperuser(username[3]));

        //tries to make an already admin an admin
        results = membershipService.addAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to make sure that username[3] is an admin
        assertTrue(memberAttributeService.isSuperuser(username[3]));

        //removes username[3] as an admin
        results = membershipService.deleteAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see that username[3] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //tries to remove an person that is not an admin
        results = membershipService.deleteAdmin(ADMIN, username[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //checks to see that username[3] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[3]));

        //checks to see that username[4] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[4]));

        //tries to make username[4] an admin but fails due to username[3] not being an admin
        try {
            membershipService.addAdmin(username[3], username[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //checks to see that username[4] is NOT an admin
        assertFalse(memberAttributeService.isSuperuser(username[4]));

        //tries to delete username[4] as an admin but fails due to username[3] not being an admin
        try {
            membershipService.deleteAdmin(username[3], username[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }

    @Test
    public void getMembershipResults() {
        List<GenericServiceResult> successResults = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            successResults.add(membershipService.getMembershipResults(ADMIN, username[i]));
        }
        for (GenericServiceResult res : successResults) {
            assertEquals(SUCCESS, res.getGroupingsServiceResult().getResultCode());
        }
        //Check no admin guard
        try {
            membershipService.getMembershipResults(username[4], username[4]);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }
    }
}
