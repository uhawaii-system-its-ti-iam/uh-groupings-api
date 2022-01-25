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
import edu.hawaii.its.api.type.UpdateTimestampResult;
import edu.hawaii.its.api.util.Dates;

import edu.internet2.middleware.grouperClient.ws.GcWebServiceError;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private String[] usernames;

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

        membershipService.addOwnerships(GROUPING, ADMIN, Arrays.asList(usernames[0]));

        groupAttributeService.changeGroupAttributeStatus(GROUPING, usernames[0], LISTSERV, true);
        groupAttributeService.changeOptInStatus(GROUPING, usernames[0], true);
        groupAttributeService.changeOptOutStatus(GROUPING, usernames[0], true);

        //Add to include.
        List<String> includeNames = new ArrayList<>();
        includeNames.add(usernames[0]);
        includeNames.add(usernames[1]);
        includeNames.add(usernames[2]);

        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, includeNames);

        // Add to basis (you cannot do this directly, so we add the user to one of the groups that makes up the basis).
        WsSubjectLookup lookup = grouperFactoryService.makeWsSubjectLookup(ADMIN);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[3]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[4]);
        grouperFactoryService.makeWsAddMemberResults(GROUPING_BASIS, lookup, usernames[5]);

        //Remove from exclude.
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[4]));
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[5]));

        //Add to exclude.
        membershipService.addGroupMembers(usernames[0], GROUPING_INCLUDE, Collections.singletonList(usernames[3]));

        //Add to basis.
        //membershipService.addGroupMember(usernames[0], GROUPING_BASIS, usernames[5]);

        //Remove ownership.
        membershipService.removeOwnerships(GROUPING, usernames[0], Arrays.asList(usernames[1]));
    }

    @Test
    public void groupOptInPermissionTest() {
        assertTrue(membershipService.isGroupCanOptIn(usernames[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(usernames[1], GROUPING_EXCLUDE));
    }
    
    @Test
    public void getMembershipResultsTest() {
        List<Membership> memberships = membershipService.getMembershipResults(usernames[0], usernames[0]);
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
        assertTrue(membershipService.isGroupCanOptOut(usernames[1], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(usernames[1], GROUPING_EXCLUDE));
    }

    @Test
    public void addRemoveSelfOptedTest() {

        //usernames[2] is not in the include, but not self opted.
        assertTrue(memberAttributeService.isMember(GROUPING_INCLUDE, usernames[2]));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, usernames[2]));

        //Add the self opted attribute for usernames[2]'s membership for the include group.
        membershipService.addSelfOpted(GROUPING_INCLUDE, usernames[2]);

        //usernames[2] should now be self opted.
        assertTrue(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, usernames[2]));

        //remove the self opted attribute for usernames[2]'s membership from the include group.
        membershipService.removeSelfOpted(GROUPING_INCLUDE, usernames[2]);

        //usernames[2] should no longer be self opted into the include.
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_INCLUDE, usernames[2]));

        //Try to add self opted attribute when not in the group.
        GroupingsServiceResult groupingsServiceResult;

        try {
            groupingsServiceResult = membershipService.addSelfOpted(GROUPING_EXCLUDE, usernames[2]);
        } catch (GroupingsServiceResultException gsre) {
            groupingsServiceResult = gsre.getGsr();
        }
        assertTrue(groupingsServiceResult.getResultCode().startsWith(FAILURE));
        assertFalse(memberAttributeService.isSelfOpted(GROUPING_EXCLUDE, usernames[2]));
    }

    @Test
    public void groupOptPermissionTest() {
        assertTrue(membershipService.isGroupCanOptOut(usernames[0], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptOut(usernames[0], GROUPING_EXCLUDE));

        assertTrue(membershipService.isGroupCanOptIn(usernames[0], GROUPING_INCLUDE));
        assertTrue(membershipService.isGroupCanOptIn(usernames[0], GROUPING_EXCLUDE));
    }

    @Test
    public void listGroupsTest() {
        //todo
    }
    @Test
    public void updateLastModifiedTest() {
        UpdateTimestampResult updateTimestampResult = membershipService.updateLastModified(GROUPING);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_INCLUDE);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_EXCLUDE);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_BASIS);
        assertNotNull(updateTimestampResult);
        updateTimestampResult = membershipService.updateLastModified(GROUPING_OWNERS);
        assertNotNull(updateTimestampResult);
    }

    @Test
    public void updateLastModifiedTimestampTest() {
        // Function updateLastModifiedTimestamp returns a complex grouper object which is wrapped into UpdateTimeResult.
        // The structure of the grouper object returned depends on three cases...
        //         i. Previous timestamp is less than new timestamp.
        //        ii. Previous timestamp is equal to new timestamp.
        //       iii. Previous timestamp is greater than new timestamp.
        // Thus, two updates are made: the epoch is first updated as the timestamp, followed by the present time.
        // This ensures case i.

        // Create a random timestamp between the epoch and now.
        LocalDateTime epoch = LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.MIDNIGHT);
        LocalDateTime randomLocalDateTime = getRandomLocalDateTimeBetween(epoch, LocalDateTime.now());
        String randomDateTime = Dates.formatDate(randomLocalDateTime, "yyyyMMdd'T'HHmm");
        // Create a timestamp of present time.
        String nowDateTime = Dates.formatDate(LocalDateTime.now(), "yyyyMMdd'T'HHmm");
        // Update grouping last modified timestamp to the random timestamp.
        UpdateTimestampResult randomDateTimeResults =
                membershipService.updateLastModifiedTimestamp(randomDateTime, GROUPING_INCLUDE);
        // Update grouping last modified timestamp to present time.
        UpdateTimestampResult nowDateTimeResults =
                membershipService.updateLastModifiedTimestamp(nowDateTime, GROUPING_INCLUDE);

        // Test for case i.
        assertNotNull(randomDateTimeResults);
        assertNotNull(nowDateTimeResults);
        assertEquals(GROUPING_INCLUDE, nowDateTimeResults.getPathOfUpdate());
        assertEquals(2, nowDateTimeResults.getTimestampUpdateArray().length);
        assertEquals(nowDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[1].getWsAttributeAssignValue().getValueSystem());
        assertEquals(randomDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[0].getWsAttributeAssignValue().getValueSystem());

        // Test for case ii.
        nowDateTimeResults = membershipService.updateLastModifiedTimestamp(nowDateTime, GROUPING_INCLUDE);
        assertNotNull(nowDateTimeResults);
        assertEquals(GROUPING_INCLUDE, nowDateTimeResults.getPathOfUpdate());
        assertEquals(1, nowDateTimeResults.getTimestampUpdateArray().length);
        assertEquals(nowDateTime,
                nowDateTimeResults.getTimestampUpdateArray()[0].getWsAttributeAssignValue().getValueSystem());
    }

    /**
     * Get a random LocalDateTime between start and end. This is a helper method for updateLastModifiedTimestampTest().
     */
    private static LocalDateTime getRandomLocalDateTimeBetween(LocalDateTime start, LocalDateTime end) {
        return LocalDateTime.of(
                getRandomNumberBetween(start.getYear(), end.getYear()),
                getRandomNumberBetween(start.getMonthValue(), end.getMonthValue()),
                getRandomNumberBetween(start.getDayOfMonth(), end.getDayOfMonth()),
                getRandomNumberBetween(start.getHour(), end.getHour()),
                getRandomNumberBetween(start.getMinute(), end.getMinute()));
    }

    /**
     * Get a random number between start and end. This is a helper method for getRandomLocalDateTimeBetween().
     */
    private static int getRandomNumberBetween(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    @Test
    public void getMembersTest() {
        String[] groupings = { GROUPING };
        Group group = groupingAssignmentService.getMembers(usernames[0], Arrays.asList(groupings)).get(GROUPING);
        List<String> usernames = group.getUsernames();

        assertTrue(usernames.contains(usernames.get(0)));
        assertTrue(usernames.contains(usernames.get(1)));
        assertTrue(usernames.contains(usernames.get(2)));
        assertTrue(usernames.contains(usernames.get(4)));
        assertTrue(usernames.contains(usernames.get(5)));
    }

    @Test
    public void addGroupingMembersTest() {
        String ownerusernames = usernames[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to include.
        List<String> validusernamess = new ArrayList<>(Arrays.asList(usernames).subList(0, 6));
        addMemberResults = membershipService.addGroupMembers(ownerusernames, GROUPING_INCLUDE, validusernamess);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getUid());
            assertNotNull(addMemberResult.getUhUuid());
            assertNotNull(addMemberResult.getName());
        }

        // Add invalid users to include.
        List<String> invalidusernamess = new ArrayList<>();
        invalidusernamess.add("zz_zzz");
        invalidusernamess.add("ffff");
        addMemberResults = membershipService.addGroupMembers(ownerusernames, GROUPING_INCLUDE, invalidusernamess);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertNull(addMemberResult.getUhUuid());
        }

        // Add valid users to exclude.
        validusernamess = new ArrayList<>(Arrays.asList(usernames).subList(0, 6));
        addMemberResults = membershipService.addGroupMembers(ownerusernames, GROUPING_EXCLUDE, validusernamess);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(SUCCESS, addMemberResult.getResult());
            assertEquals(GROUPING_EXCLUDE, addMemberResult.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, addMemberResult.getPathOfRemoved());
            assertNotNull(addMemberResult.getUid());
            assertNotNull(addMemberResult.getUhUuid());
            assertNotNull(addMemberResult.getName());
        }

        // Add invalid users to include.
        List<String> invalidusernamessForExclude = new ArrayList<>();
        invalidusernamessForExclude.add("zz_zzz");
        invalidusernamessForExclude.add("ffff");
        addMemberResults =
                membershipService.addGroupMembers(ownerusernames, GROUPING_EXCLUDE, invalidusernamessForExclude);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertEquals(FAILURE, addMemberResult.getResult());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertNull(addMemberResult.getUhUuid());
        }

        // A non-owner attempts to add members.
        try {
            membershipService.addGroupMembers("zz_zz", GROUPING_INCLUDE, validusernamess);
        } catch (AccessDeniedException e) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(e.getMessage()));
        }

        List<String> invalidUsers = new ArrayList<>();
        invalidUsers.add("zz_zzzzz");
        invalidUsers.add("aaaaaaa");

        addMemberResults = membershipService.addGroupMembers(ownerusernames, GROUPING_INCLUDE, invalidUsers);
        for (AddMemberResult addMemberResult : addMemberResults) {
            assertFalse(addMemberResult.isUserWasRemoved());
            assertNull(addMemberResult.getName());
            assertNull(addMemberResult.getUid());
            assertEquals(FAILURE, addMemberResult.getResult());
        }

        // A group path ending in anything other than include or exclude should 404.
        try {
            membershipService.addGroupMembers(ownerusernames, GROUPING_OWNERS, validusernamess);
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }
    }

    @Test
    public void addIncludeMembersTest() {
        String ownerusernames = usernames[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to include.
        List<String> validusernamess = new ArrayList<>(Arrays.asList(usernames).subList(0, 6));
        addMemberResults = membershipService.addIncludeMembers(ownerusernames, GROUPING, validusernamess);
        Iterator<String> iter = validusernamess.iterator();
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
        String ownerusernames = usernames[0];
        List<AddMemberResult> addMemberResults;

        // Add valid users to exclude.
        List<String> validusernamess = new ArrayList<>(Arrays.asList(usernames).subList(0, 6));
        addMemberResults = membershipService.addExcludeMembers(ownerusernames, GROUPING, validusernamess);
        Iterator<String> iter = validusernamess.iterator();
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

        String ownerusernames = usernames[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableusernamess = new ArrayList<>(Collections.singletonList(usernames[0]));

        // Remove a single member.
        removeMemberResults =
                membershipService.removeGroupMembers(ownerusernames, GROUPING_INCLUDE, removableusernamess);

        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertTrue(removeMemberResult.isUserWasRemoved());
            assertEquals(SUCCESS, removeMemberResult.getResult());
            assertNotNull(removeMemberResult.getUhUuid());
            assertNotNull(removeMemberResult.getName());
            assertNotNull(removeMemberResult.getUid());
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
        }

        // Remove multiple members.
        removableusernamess = new ArrayList<>(Arrays.asList(usernames).subList(1, 6));
        removeMemberResults =
                membershipService.removeGroupMembers(ownerusernames, GROUPING_INCLUDE, removableusernamess);
        Iterator<String> removableusernamessIter = removableusernamess.iterator();
        Iterator<RemoveMemberResult> removedMemberResultsIter = removeMemberResults.iterator();

        while (removableusernamessIter.hasNext() && removedMemberResultsIter.hasNext()) {
            RemoveMemberResult result = removedMemberResultsIter.next();
            String uid = removableusernamessIter.next();
            assertTrue(result.isUserWasRemoved());
            assertEquals(SUCCESS, result.getResult());
            assertEquals(uid, result.getUid());
            assertEquals(uid, result.getUhUuid());
            assertNotNull(result.getUhUuid());
            assertNotNull(result.getUid());
            assertNotNull(result.getName());
            assertEquals(GROUPING_INCLUDE, result.getPathOfRemoved());
        }

        // Try to remove non-members, the list of removableusernamess has already been removed above, thus attempting to
        // remove them again should fail.
        removeMemberResults =
                membershipService.removeGroupMembers(ownerusernames, GROUPING_INCLUDE, removableusernamess);
        removedMemberResultsIter = removeMemberResults.iterator();

        while (removedMemberResultsIter.hasNext()) {
            RemoveMemberResult result = removedMemberResultsIter.next();
            assertFalse(result.isUserWasRemoved());
            assertEquals(FAILURE, result.getResult());
        }

        List<String> invalidUsers = new ArrayList<>();
        invalidUsers.add("zzz_zz_zz");
        invalidUsers.add("aaa_aaaa");

        removeMemberResults = membershipService.removeGroupMembers(ownerusernames, GROUPING_INCLUDE, invalidUsers);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertFalse(removeMemberResult.isUserWasRemoved());
            assertNull(removeMemberResult.getName());
            assertNull(removeMemberResult.getUid());
            assertEquals(FAILURE, removeMemberResult.getResult());
        }

        try {
            membershipService.removeGroupMembers(ownerusernames, GROUPING_OWNERS, removableusernamess);
        } catch (GcWebServiceError e) {
            assertEquals("404: Invalid group path.", e.getContainerResponseObject().toString());
        }

    }

    @Test
    public void removeIncludeMembersTest() {
        String ownerusernames = usernames[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableusernamess = new ArrayList<>(Collections.singletonList(usernames[0]));
        removeMemberResults =
                membershipService.removeIncludeMembers(ownerusernames, GROUPING, removableusernamess);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(GROUPING_INCLUDE, removeMemberResult.getPathOfRemoved());
        }
    }

    @Test
    public void removeExcludeMembersTest() {
        String ownerusernames = usernames[0];
        List<RemoveMemberResult> removeMemberResults;
        List<String> removableusernamess = new ArrayList<>(Collections.singletonList(usernames[0]));
        removeMemberResults =
                membershipService.removeExcludeMembers(ownerusernames, GROUPING, removableusernamess);
        for (RemoveMemberResult removeMemberResult : removeMemberResults) {
            assertEquals(GROUPING_EXCLUDE, removeMemberResult.getPathOfRemoved());
        }
    }

    @Test
    public void optInTest() {
        String ownerusernames = usernames[0];
        List<AddMemberResult> optResults;

        optResults = membershipService.optIn(ownerusernames, GROUPING, ownerusernames);
        for (AddMemberResult optResult : optResults) {
            assertEquals(GROUPING_INCLUDE, optResult.getPathOfAdd());
            assertEquals(GROUPING_EXCLUDE, optResult.getPathOfRemoved());
            assertTrue(memberAttributeService.isMember(GROUPING, ownerusernames));
        }
    }

    @Test
    public void optOutTest() {
        String ownerusernames = usernames[0];
        List<AddMemberResult> optResults;

        optResults = membershipService.optOut(ownerusernames, GROUPING, ownerusernames);
        for (AddMemberResult optResult : optResults) {
            assertEquals(GROUPING_EXCLUDE, optResult.getPathOfAdd());
            assertEquals(GROUPING_INCLUDE, optResult.getPathOfRemoved());
            assertFalse(memberAttributeService.isMember(GROUPING, ownerusernames));
        }
    }

    //Add admin and remove admin in one test
    @Test
    public void adminTest() {

        GroupingsServiceResult results;

        //checks to see that usernames[3] is NOT an admin
        results = membershipService.removeAdmin(ADMIN, usernames[3]);

        //makes usernames[3] an admin
        results = membershipService.addAdmin(ADMIN, usernames[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to make an already admin an admin
        results = membershipService.addAdmin(ADMIN, usernames[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //removes usernames[3] as an admin
        results = membershipService.removeAdmin(ADMIN, usernames[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to remove an person that is not an admin
        results = membershipService.removeAdmin(ADMIN, usernames[3]);
        assertTrue(results.getResultCode().startsWith(SUCCESS));

        //tries to make usernames[4] an admin but fails due to usernames[3] not being an admin
        try {
            membershipService.addAdmin(usernames[3], usernames[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }

        //tries to remove usernames[4] as an admin but fails due to usernames[3] not being an admin
        try {
            membershipService.removeAdmin(usernames[3], usernames[4]);
        } catch (AccessDeniedException ade) {
            assertThat(INSUFFICIENT_PRIVILEGES, is(ade.getMessage()));
        }
    }
}