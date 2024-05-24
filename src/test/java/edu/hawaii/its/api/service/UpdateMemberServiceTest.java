package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.exception.UhMemberNotFoundException;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class UpdateMemberServiceTest {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private UpdateMemberService updateMemberService;

    private PropertyLocator propertyLocator;

    @SpyBean
    private GrouperService grouperService;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    private String groupPath = "group-path";

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void addAdminTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(1));

        json = propertyLocator.find("ws.get.subject.result.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(0));

        json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], groupPath);
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(1), GROUPING_ADMINS, TEST_UIDS.get(0));

        assertNotNull(updateMemberService.addAdminMember(TEST_UIDS.get(1), TEST_UIDS.get(0)));

        json = propertyLocator.find("ws.get.subject.result.uid.failure");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects("bogus-identifier");

        assertThrows(UhMemberNotFoundException.class,
                () -> updateMemberService.addAdminMember(TEST_UIDS.get(1), "bogus-identifier"));
    }

    @Test
    public void removeAdminTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(1));

        json = propertyLocator.find("ws.get.subject.result.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults.getResults()[0], groupPath);
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(1), GROUPING_ADMINS, TEST_UIDS.get(0));

        assertNotNull(updateMemberService.removeAdminMember(TEST_UIDS.get(1), TEST_UIDS.get(0)));

        json = propertyLocator.find("ws.get.subject.result.uid.failure");
        wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects("bogus-identifier");

        assertThrows(UhMemberNotFoundException.class,
                () -> updateMemberService.removeAdminMember(TEST_UIDS.get(1), "bogus-identifier"));
    }

    @Test
    public void addOwnershipsTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        json = propertyLocator.find("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifiers);

        assertNotNull(updateMemberService.addOwnerships(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }


    @Test
    public void addOwnershipTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subject.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(1));

        json = propertyLocator.find("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], groupPath);
        String validIdentifier = subjectService.getValidUhUuid(TEST_UIDS.get(1));
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifier);

        assertNotNull(updateMemberService.addOwnership(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));

    }

    @Test
    public void removeOwnershipsTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifiers);

        assertNotNull(updateMemberService.removeOwnerships(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeOwnershipTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subject.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        assertNotNull(subjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS.get(1));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResults.getResults()[0], groupPath);
        String validIdentifier = subjectService.getValidUhUuid(TEST_UIDS.get(1));
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.OWNERS.value(), validIdentifier);

        assertNotNull(updateMemberService.removeOwnership(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));

    }

    @Test
    public void addIncludeMembersTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), validIdentifiers);

        json = propertyLocator.find("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), validIdentifiers);

        assertNotNull(updateMemberService.addIncludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void addExcludeMembersTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults subjectsResults = new SubjectsResults(wsGetSubjectsResults);
        doReturn(subjectsResults).when(grouperService).getSubjects(TEST_UIDS);

        List<String> validIdentifiers = subjectService.getValidUhUuids(TEST_UIDS);
        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), validIdentifiers);

        json = propertyLocator.find("ws.add.member.results.failure");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMembersResults = new AddMembersResults(wsAddMemberResults);
        doReturn(addMembersResults).when(grouperService)
                .addMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), validIdentifiers);

        assertNotNull(updateMemberService.addExcludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeIncludeMembersTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS);

        assertNotNull(updateMemberService.removeIncludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeIncludeMemberTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));

        assertNotNull(updateMemberService.removeIncludeMember(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void removeExcludeMembersTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        assertNotNull(removeMembersResults);
        doReturn(removeMembersResults).when(grouperService)
                .removeMembers(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS);

        assertNotNull(updateMemberService.removeExcludeMembers(TEST_UIDS.get(0), groupPath, TEST_UIDS));
    }

    @Test
    public void removeExcludeMemberTest() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        assertNotNull(findGroupsResults);
        doReturn(findGroupsResults).when(grouperService).findGroupsResults(groupPath);

        json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        assertNotNull(updateMemberService.removeExcludeMember(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void optInTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], groupPath);
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));

        assertNotNull(updateMemberService.optIn(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

    @Test
    public void optOutTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService).hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        json = propertyLocator.find("ws.delete.member.results.failure");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        assertNotNull(wsDeleteMemberResults);
        WsDeleteMemberResult wsDeleteMemberResult = wsDeleteMemberResults.getResults()[0];
        assertNotNull(wsDeleteMemberResult);
        RemoveMemberResult removeMemberResult = new RemoveMemberResult(wsDeleteMemberResult, "group-path");
        assertNotNull(removeMemberResult);
        doReturn(removeMemberResult).when(grouperService)
                .removeMember(TEST_UIDS.get(0), groupPath + GroupType.INCLUDE.value(), TEST_UIDS.get(1));

        json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMemberResult addMemberResult = new AddMemberResult(wsAddMemberResults.getResults()[0], groupPath);
        assertNotNull(addMemberResult);
        doReturn(addMemberResult).when(grouperService)
                .addMember(TEST_UIDS.get(0), groupPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(1));

        assertNotNull(updateMemberService.optOut(TEST_UIDS.get(0), groupPath, TEST_UIDS.get(1)));
    }

}
