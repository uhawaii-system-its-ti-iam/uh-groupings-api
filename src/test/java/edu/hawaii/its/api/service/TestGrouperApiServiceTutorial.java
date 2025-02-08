//package edu.hawaii.its.api.service;
///**
// * DISCLAIMER:
// * This is NOT a normal integration test.
// * This is a learning tool that is used to help better understand the Grouper API.
// * <p>
// * These tests below do not provide any extra code coverage for our project, but they
// * provide a guide as to how each Grouper API function works and what should be expected of them.
// * Although the functions in grouperService use grouper client (Gc) and web service (Ws)
// * files that were not written by us, the functions itself are used in other places throughout
// * the API, so it will also be beneficial if you look through how these functions are used in
// * the context of our code as well. There is also documentation on how each Gc and Ws function
// * works here: https://spaces.at.internet2.edu/display/Grouper/Grouper+Web+Services
// */
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//import java.util.Arrays;
//import java.util.List;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import edu.hawaii.its.api.configuration.SpringBootWebApplication;
//import edu.hawaii.its.api.groupings.GroupingMembers;
//import edu.hawaii.its.api.type.OptType;
//import edu.hawaii.its.api.type.PrivilegeType;
//import edu.hawaii.its.api.wrapper.AddMemberResult;
//import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
//import edu.hawaii.its.api.wrapper.AttributesResult;
//import edu.hawaii.its.api.wrapper.GetGroupsResults;
//import edu.hawaii.its.api.wrapper.GetMembersResult;
//import edu.hawaii.its.api.wrapper.GetMembersResults;
//import edu.hawaii.its.api.wrapper.Group;
//import edu.hawaii.its.api.wrapper.GroupAttribute;
//import edu.hawaii.its.api.wrapper.GroupAttributeResults;
//import edu.hawaii.its.api.wrapper.HasMemberResult;
//import edu.hawaii.its.api.wrapper.HasMembersResults;
//import edu.hawaii.its.api.wrapper.RemoveMemberResult;
//import edu.hawaii.its.api.wrapper.Subject;
//import edu.hawaii.its.api.wrapper.SubjectsResults;
//
//@ActiveProfiles("integrationTest")
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@SpringBootTest(classes = { SpringBootWebApplication.class })
//public class TestGrouperApiServiceTutorial {
//    @Value("${groupings.api.test.grouping_many}")
//    private String GROUPING;
//
//    @Value("${groupings.api.test.grouping_many_include}")
//    private String GROUPING_INCLUDE;
//
//    @Value("${groupings.api.test.grouping_many_exclude}")
//    private String GROUPING_EXCLUDE;
//
//    @Value("${groupings.api.test.grouping_many_owners}")
//    private String GROUPING_OWNERS;
//
//    @Value("${groupings.api.test.admin_user}")
//    private String ADMIN;
//
//    @Value("${groupings.api.assign_type_group}")
//    private String ASSIGN_TYPE_GROUP;
//
//    @Value("${groupings.api.trio}")
//    private String TRIO;
//
//    @Value("${groupings.api.grouping_admins}")
//    private String GROUPING_ADMINS;
//
//    private List<String> testUids;
//    private List<String> testUhUuids;
//
//    @Autowired
//    private GrouperService grouperService;
//
//    @Autowired
//    private MemberService memberService;
//
//    @Autowired
//    private UhIdentifierGenerator uhIdentifierGenerator;
//
//    @BeforeAll
//    public void init() {
//        assertTrue(memberService.isAdmin(ADMIN));
//
//        GroupingMembers testGroupingMembers = uhIdentifierGenerator.getRandomMembers(5);
//        testUids = testGroupingMembers.getUids();
//        testUhUuids = testGroupingMembers.getUhUuids();
//
//        testUids.forEach(testUid -> {
//            grouperService.removeMember(ADMIN, GROUPING_ADMINS, testUid);
//            grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUid);
//            grouperService.removeMember(ADMIN, GROUPING_EXCLUDE, testUid);
//            grouperService.removeMember(ADMIN, GROUPING_OWNERS, testUid);
//
//            assertFalse(memberService.isOwner(GROUPING, testUid));
//            assertFalse(memberService.isMember(GROUPING_INCLUDE, testUid));
//            assertFalse(memberService.isMember(GROUPING_EXCLUDE, testUid));
//            assertFalse(memberService.isAdmin(testUid));
//        });
//    }
//
//    @Test
//    public void addMemberTest() {
//        // With uh uids.
//        AddMemberResult addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//        assertNotNull(addMemberResult);
//        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUids.get(0)));
//
//        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
//        assertNotNull(addMemberResult);
//        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUids.get(1)));
//        //// Clean up
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
//
//        // With uh numbers.
//        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        assertNotNull(addMemberResult);
//        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(0)));
//
//        addMemberResult = grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
//        assertNotNull(addMemberResult);
//        assertTrue(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(1)));
//        //// Clean up
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
//    }
//
//    @Test
//    public void removeMemberTest() {
//        // With uh uids.
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
//        RemoveMemberResult removeMemberResult =
//                grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//        assertNotNull(removeMemberResult);
//        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUids.get(0)));
//        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(1));
//        assertNotNull(removeMemberResult);
//        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUids.get(1)));
//
//        // With uh numbers.
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
//        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        assertNotNull(removeMemberResult);
//        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(0)));
//        removeMemberResult = grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(1));
//        assertNotNull(removeMemberResult);
//        assertFalse(memberService.isMember(GROUPING_INCLUDE, testUhUuids.get(1)));
//    }
//
//    @Test
//    public void groupAttributeResults() {
//        // All group with these attributes
//        GroupAttributeResults groupAttributeResults =
//                grouperService.groupAttributeResults(Arrays.asList(TRIO, OptType.IN.value()));
//        assertNotNull(groupAttributeResults);
//        List<GroupAttribute> groupAttributes = groupAttributeResults.getGroupAttributes();
//        List<AttributesResult> attributesResults = groupAttributeResults.getAttributesResults();
//        groupAttributes.forEach(Assertions::assertNotNull);
//        attributesResults.forEach(Assertions::assertNotNull);
//        assertEquals(attributesResults.size(), 2);
//        attributesResults.forEach(
//                defName -> assertTrue(defName.getName().equals(TRIO) || defName.getName().equals(OptType.IN.value())));
//        groupAttributes.forEach(groupAttribute -> assertEquals(ASSIGN_TYPE_GROUP, groupAttribute.getAssignType()));
//        groupAttributes.forEach(groupAttribute -> assertTrue(groupAttribute.getAttributeName().equals(TRIO) ||
//                groupAttribute.getAttributeName().equals(OptType.IN.value())));
//
//        // Attributes of a single grouping.
//        groupAttributeResults = grouperService.groupAttributeResult(GROUPING);
//        assertNotNull(groupAttributes);
//        groupAttributes = groupAttributeResults.getGroupAttributes();
//        attributesResults = groupAttributeResults.getAttributesResults();
//        groupAttributes.forEach(Assertions::assertNotNull);
//        attributesResults.forEach(Assertions::assertNotNull);
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getGroupPath(), GROUPING));
//
//        // All Groups and there attributes.
//        groupAttributeResults = grouperService.groupAttributeResults(TRIO);
//        assertNotNull(groupAttributeResults);
//        List<Group> groups = groupAttributeResults.getGroups();
//        groupAttributes = groupAttributeResults.getGroupAttributes();
//        attributesResults = groupAttributeResults.getAttributesResults();
//        groupAttributes.forEach(Assertions::assertNotNull);
//        attributesResults.forEach(Assertions::assertNotNull);
//        groups.forEach(Assertions::assertNotNull);
//        assertEquals(attributesResults.size(), 1);
//        assertEquals(groupAttributes.get(0).getAttributeName(), TRIO);
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAttributeName(), TRIO));
//
//        // Check if a group has a certain attribute.
//        groupAttributeResults = grouperService.groupAttributeResults(TRIO, GROUPING);
//        assertNotNull(groupAttributeResults);
//        groupAttributes = groupAttributeResults.getGroupAttributes();
//        attributesResults = groupAttributeResults.getAttributesResults();
//        groupAttributes.forEach(Assertions::assertNotNull);
//        attributesResults.forEach(Assertions::assertNotNull);
//        assertEquals(attributesResults.size(), 1);
//        assertEquals(attributesResults.get(0).getName(), TRIO);
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAssignType(), ASSIGN_TYPE_GROUP));
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getAttributeName(), TRIO));
//        groupAttributes.forEach(groupAttribute -> assertEquals(groupAttribute.getGroupPath(), GROUPING));
//    }
//
//    @Test
//    public void hasMemberResultsTest() {
//        // Using uh numbers (one that is a member)
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        HasMembersResults hasMemberResultsIsMember =
//                grouperService.hasMemberResults(GROUPING_INCLUDE, testUhUuids.get(0));
//        assertNotNull(hasMemberResultsIsMember);
//        List<HasMemberResult> memberResultsIsMember = hasMemberResultsIsMember.getResults();
//        assertEquals(hasMemberResultsIsMember.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(memberResultsIsMember.size(), 1);
//        assertEquals(memberResultsIsMember.get(0).getUhUuid(), testUhUuids.get(0));
//        assertEquals(memberResultsIsMember.get(0).getResultCode(), "IS_MEMBER");
//        // Using uh numbers (one that is not a member)
//        HasMembersResults hasMemberResultsNonMember =
//                grouperService.hasMemberResults(GROUPING_INCLUDE, testUhUuids.get(1));
//        assertNotNull(hasMemberResultsNonMember);
//        List<HasMemberResult> memberResultsNonMember = hasMemberResultsNonMember.getResults();
//        assertEquals(hasMemberResultsNonMember.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(memberResultsNonMember.size(), 1);
//        assertEquals(memberResultsNonMember.get(0).getUhUuid(), testUhUuids.get(1));
//        assertEquals(memberResultsNonMember.get(0).getResultCode(), "IS_NOT_MEMBER");
//
//        // Using uh uids (one that is a member)
//        grouperService.addMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//        hasMemberResultsIsMember =
//                grouperService.hasMemberResults(GROUPING_INCLUDE, testUids.get(0));
//        assertNotNull(hasMemberResultsIsMember);
//        memberResultsIsMember = hasMemberResultsIsMember.getResults();
//        assertEquals(hasMemberResultsIsMember.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(memberResultsIsMember.size(), 1);
//        assertEquals(memberResultsIsMember.get(0).getUid(), testUids.get(0));
//        assertEquals(memberResultsIsMember.get(0).getResultCode(), "IS_MEMBER");
//        // Using uh uids (one that is not a member)
//        hasMemberResultsNonMember =
//                grouperService.hasMemberResults(GROUPING_INCLUDE, testUids.get(1));
//        assertNotNull(hasMemberResultsNonMember);
//        memberResultsNonMember = hasMemberResultsNonMember.getResults();
//        assertEquals(hasMemberResultsNonMember.getGroupPath(), GROUPING_INCLUDE);
//        assertEquals(memberResultsNonMember.size(), 1);
//        assertEquals(memberResultsNonMember.get(0).getUid(), testUids.get(1));
//        assertEquals(memberResultsNonMember.get(0).getResultCode(), "IS_NOT_MEMBER");
//
//        // cleanup
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUhUuids.get(0));
//        grouperService.removeMember(ADMIN, GROUPING_INCLUDE, testUids.get(0));
//    }
//
//    @Test
//    public void assignGrouperPrivilegesLiteResult() {
//        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult =
//                grouperService.assignGrouperPrivilegesResult(ADMIN, GROUPING, PrivilegeType.IN.value(), ADMIN,true);
//        assertNotNull(assignGrouperPrivilegesResult);
//        Group group = assignGrouperPrivilegesResult.getGroup();
//        assertNotNull(group);
//        assertEquals(GROUPING, group.getGroupPath());
//        assertEquals(PrivilegeType.IN.value(), assignGrouperPrivilegesResult.getPrivilegeName());
//        assertEquals("access", assignGrouperPrivilegesResult.getPrivilegeType());
//        assertEquals(ADMIN, assignGrouperPrivilegesResult.getSubject().getUid());
//    }
//
//    @Test
//    public void getMembersResults() {
//        GetMembersResults getMembersResults = grouperService.getMembersResults(
//                ADMIN,
//                Arrays.asList(GROUPING_INCLUDE, GROUPING_EXCLUDE),
//                null,
//                null,
//                "name",
//                true);
//        assertNotNull(getMembersResults);
//        assertEquals("SUCCESS", getMembersResults.getResultCode());
//        List<GetMembersResult> membersResults = getMembersResults.getMembersResults();
//        assertTrue(membersResults.stream().allMatch(getMembersResult ->
//                getMembersResult.getGroup().getGroupPath().equals(GROUPING_INCLUDE) ||
//                        getMembersResult.getGroup().getGroupPath().equals(GROUPING_EXCLUDE)));
//    }
//
//    @Test
//    public void groupsResultsTest() {
//        GetGroupsResults getGroupsResultsUids = grouperService.getGroupsResults(testUids.get(0));
//        GetGroupsResults getGroupsResultsNumbers = grouperService.getGroupsResults(testUhUuids.get(0));
//        assertNotNull(getGroupsResultsNumbers);
//        assertFalse(getGroupsResultsNumbers.getGroups().isEmpty());
//        assertEquals(testUhUuids.get(0), getGroupsResultsNumbers.getSubject().getUhUuid());
//
//        assertNotNull(getGroupsResultsUids);
//        assertFalse(getGroupsResultsUids.getGroups().isEmpty());
//        assertEquals(testUids.get(0), getGroupsResultsUids.getSubject().getUid());
//
//    }
//
//    @Test
//    public void subjectsResultsTest() {
//        SubjectsResults subjectsResults = grouperService.getSubjects(ADMIN);
//        List<Subject> subjects = subjectsResults.getSubjects();
//        assertEquals(1, subjects.size());
//        assertEquals(ADMIN, subjects.get(0).getUid());
//    }
//
//}