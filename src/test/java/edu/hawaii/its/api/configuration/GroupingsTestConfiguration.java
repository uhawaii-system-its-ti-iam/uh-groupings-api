package edu.hawaii.its.api.configuration;

import org.springframework.boot.test.context.TestConfiguration;

import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributeResult;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.AttributeAssignmentsResults;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.Group;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupsResults;
import edu.hawaii.its.api.wrapper.HasMemberResult;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssign;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindAttributeDefNamesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@TestConfiguration
public class GroupingsTestConfiguration {

    private final PropertyLocator propertyLocator =
            new PropertyLocator("src/test/resources", "grouper.test.properties");

    private <T> T getWsResultTestData(String propertyName, Class<T> type) {
        String json = propertyLocator.find(propertyName);
        return JsonUtil.asObject(json, type);
    }

    public HasMembersResults hasMemberResultsIsMembersUidTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.members.uid", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsMembersUhuuidTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.members.uhuuid", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsNotMembersUidTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.not.members.uid", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsMembersBasisTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.members.basis", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsNotMembersBasisTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.not.members.basis", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsMembersFailureTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.members.failure", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsIsNotMembersTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.results.is.not.members", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsNullGroupTestData() {
        return new HasMembersResults(getWsResultTestData("ws.has.member.results.null.group", WsHasMemberResults.class));
    }

    public HasMembersResults hasMemberResultsMixedTestData() {
        WsHasMemberResults wsMemberResults =
                getWsResultTestData("ws.has.member.results.is.members.uid", WsHasMemberResults.class);
        WsHasMemberResults wsNonMemberResults =
                getWsResultTestData("ws.has.member.results.is.not.members.uid", WsHasMemberResults.class);

        WsHasMemberResults wsMixedResults = new WsHasMemberResults();
        wsMixedResults.setResultMetadata(wsMemberResults.getResultMetadata());
        wsMixedResults.setWsGroup(wsMemberResults.getWsGroup());

        int totalResults = 5;
        WsHasMemberResult[] mixedResults = new WsHasMemberResult[totalResults];
        mixedResults[0] = wsMemberResults.getResults()[0];
        mixedResults[1] = wsMemberResults.getResults()[1];
        mixedResults[2] = wsNonMemberResults.getResults()[0];
        mixedResults[3] = wsMemberResults.getResults()[2];
        mixedResults[4] = wsNonMemberResults.getResults()[1];

        wsMixedResults.setResults(mixedResults);

        return new HasMembersResults(wsMixedResults);
    }

    public HasMemberResult hasMemberResultNullSubjectResultCodeTestData() {
        return new HasMemberResult(
                getWsResultTestData("ws.has.member.result.null.subject.result.code", WsHasMemberResult.class));
    }

    public AddMembersResults addMemberResultsSuccessTestData() {
        return new AddMembersResults(getWsResultTestData("ws.add.member.results.success", WsAddMemberResults.class));
    }

    public AddMembersResults addMemberResultsFailureTestData() {
        return new AddMembersResults(getWsResultTestData("ws.add.member.results.failure", WsAddMemberResults.class));
    }

    public AddMemberResult addMemberResultSuccessTestData() {
        return new AddMemberResult(
                getWsResultTestData("ws.add.member.results.success", WsAddMemberResults.class).getResults()[0],
                "group-path");
    }

    public AddMemberResult addMemberResultFailureTestData() {
        return new AddMemberResult(
                getWsResultTestData("ws.add.member.results.failure", WsAddMemberResults.class).getResults()[0],
                "group-path");
    }

    public SubjectsResults getSubjectsResultsSuccessTestData() {
        return new SubjectsResults(getWsResultTestData("ws.get.subjects.results.success", WsGetSubjectsResults.class));
    }

    public SubjectsResults getSubjectsResultsFailureTestData() {
        return new SubjectsResults(getWsResultTestData("ws.get.subjects.results.failure", WsGetSubjectsResults.class));
    }

    public SubjectsResults getSubjectsResultsEmptyTestData() {
        return new SubjectsResults(getWsResultTestData("ws.get.subjects.results.empty", WsGetSubjectsResults.class));
    }

    public SubjectsResults getSubjectResultSuccessTestData() {
        return new SubjectsResults(getWsResultTestData("ws.get.subject.result.success", WsGetSubjectsResults.class));
    }

    public SubjectsResults getSubjectResultUidFailureTestData() {
        return new SubjectsResults(
                getWsResultTestData("ws.get.subject.result.uid.failure", WsGetSubjectsResults.class));
    }

    public FindGroupsResults findGroupsResultsDescriptionTestData() {
        return new FindGroupsResults(getWsResultTestData("find.groups.results.description", WsFindGroupsResults.class));
    }

    public FindGroupsResults findGroupsResultsEmptyDescriptionTestData() {
        return new FindGroupsResults(
                getWsResultTestData("find.groups.results.empty.description", WsFindGroupsResults.class));
    }

    public FindGroupsResults findGroupsResultsNullDescriptionTestData() {
        return new FindGroupsResults(
                getWsResultTestData("find.groups.results.null.description", WsFindGroupsResults.class));
    }

    public FindGroupsResults findGroupsResultsFailureTestData() {
        return new FindGroupsResults(getWsResultTestData("find.groups.results.failure", WsFindGroupsResults.class));
    }

    public Subject subjectSuccessUidTestData() {
        return new Subject(getWsResultTestData("ws.subject.success.uid", WsSubject.class));
    }

    public Subject subjectSuccessUhuuidTestData() {
        return new Subject(getWsResultTestData("ws.subject.success.uhuuid", WsSubject.class));
    }

    public Subject subjectUidNotFoundTestData() {
        return new Subject(getWsResultTestData("ws.subject.subject.uid.not.found", WsSubject.class));
    }

    public Subject subjectUhuuidNotFoundTestData() {
        return new Subject(getWsResultTestData("ws.subject.subject.uhuuid.not.found", WsSubject.class));
    }

    public Subject subjectSuccessNullValuesTestData() {
        return new Subject(getWsResultTestData("ws.subject.success.null.values", WsSubject.class));
    }

    public Group groupSuccessTestData() {
        WsGroup wsGroup = getWsResultTestData("ws.group", WsGroup.class);
        return new Group(wsGroup);
    }

    public AssignAttributeResult assignAttributesResultsTurnOffOptInSuccessTestData() {

        WsAssignAttributesResults wsAssignAttributesResults =
                getWsResultTestData(
                        "ws.assign.attributes.results.turn.off.opt.in.success",
                        WsAssignAttributesResults.class);

        return new AssignAttributeResult(
                wsAssignAttributesResults.getWsAttributeAssignResults()[0]);
    }

    public AssignAttributeResult assignAttributesResultsNullAssignAttributeResultTestData() {

        WsAssignAttributesResults wsAssignAttributesResults =
                getWsResultTestData(
                        "ws.assign.attributes.results.null.assign.attribute.result",
                        WsAssignAttributesResults.class);

        return new AssignAttributeResult(
                wsAssignAttributesResults.getWsAttributeAssignResults()[0]);
    }

    public AssignAttributesResults assignAttributesResultsChangedTrueTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.changed.true", WsAssignAttributesResults.class));
    }

    public RemoveMembersResults deleteMemberResultsSuccessTestData() {
        return new RemoveMembersResults(
                getWsResultTestData("ws.delete.member.results.success", WsDeleteMemberResults.class));
    }

    public RemoveMembersResults deleteMemberResultsFailureTestData() {
        return new RemoveMembersResults(
                getWsResultTestData("ws.delete.member.results.failure", WsDeleteMemberResults.class));
    }

    public RemoveMemberResult deleteMemberResultSuccessTestData() {
        return new RemoveMemberResult(
                getWsResultTestData("ws.delete.member.results.success", WsDeleteMemberResults.class).getResults()[0],
                "group-path");
    }

    public RemoveMemberResult deleteMemberResultFailureTestData() {
        return new RemoveMemberResult(
                getWsResultTestData("ws.delete.member.results.failure", WsDeleteMemberResults.class).getResults()[0],
                "group-path");
    }

    public GroupAttributeResults getAttributeAssignmentResultsSuccessTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.success",
                WsGetAttributeAssignmentsResults.class));
    }

    public AssignGrouperPrivilegesResult assignGrouperPrivilegesResultsSuccessTestData() {
        return new AssignGrouperPrivilegesResult(getWsResultTestData("ws.assign.grouper.privileges.results.success",
                WsAssignGrouperPrivilegesLiteResult.class));
    }

    public GetGroupsResults getGroupsResultsSuccessTestData() {
        return new GetGroupsResults(getWsResultTestData("ws.get.groups.results.success", WsGetGroupsResults.class));
    }

    public GetGroupsResults getGroupsResultsEmptyGroupsTestData() {
        return new GetGroupsResults(
                getWsResultTestData("ws.get.groups.results.empty.groups", WsGetGroupsResults.class));
    }

    public GetGroupsResults getGroupsResultsEmptyResultsTestData() {
        WsGetGroupsResults wsGetGroupsResults =
                getWsResultTestData("ws.empty.results", WsGetGroupsResults.class);
        return new GetGroupsResults(wsGetGroupsResults);
    }

    public GroupsResults groupsResultsSuccessTestData() {
        WsGetGroupsResults wsGetGroupsResults =
                getWsResultTestData("groups.results", WsGetGroupsResults.class);
        return new GroupsResults(wsGetGroupsResults);
    }

    public GroupsResults groupsResultsEmptyResultsTestData() {
        WsGetGroupsResults wsGetGroupsResults =
                getWsResultTestData("groups.results.empty.results", WsGetGroupsResults.class);
        return new GroupsResults(wsGetGroupsResults);
    }

    public GroupsResults groupsResultsEmptyGroupsTestData() {
        WsGetGroupsResults wsGetGroupsResults =
                getWsResultTestData("groups.results.empty.groups", WsGetGroupsResults.class);
        return new GroupsResults(wsGetGroupsResults);
    }

    public GetMembersResults getMembersResultsSuccessTestData() {
        WsGetMembersResults
                wsGetMembersResults = getWsResultTestData("ws.get.members.results.success", WsGetMembersResults.class);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GetMembersResults getMembersResultsNoMembersTestData() {
        WsGetMembersResults wsGetMembersResults =
                getWsResultTestData("ws.get.members.results.no.members", WsGetMembersResults.class);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GetMembersResults getMembersResultsNullTestData() {
        WsGetMembersResults wsGetMembersResults =
                getWsResultTestData("ws.get.members.results.null", WsGetMembersResults.class);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GetMembersResults getMembersResultsEmptyResultsTestData() {
        WsGetMembersResults wsGetMembersResults =
                getWsResultTestData("ws.empty.results", WsGetMembersResults.class);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GetMembersResults getMembersResultsSuccessMultipleGroupsTestData() {
        WsGetMembersResults wsGetMembersResults =
                getWsResultTestData("ws.get.members.results.success.multiple.groups", WsGetMembersResults.class);
        return new GetMembersResults(wsGetMembersResults);
    }

    public GroupingSyncDestination attributeDescriptionTestData() {
        return getWsResultTestData("ws.attribute.description", GroupingSyncDestination.class);
    }

    public FindAttributesResults attributeDefNameResultsSuccessTestData() {
        return new FindAttributesResults(
                getWsResultTestData("ws.attribute.def.name.results.success", WsFindAttributeDefNamesResults.class));
    }

    public GroupAttributeResults getAttributeAssignmentResultsOptInOnOptOutOnTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.optIn-on.optOut-on",
                WsGetAttributeAssignmentsResults.class));
    }

    public GroupAttributeResults getAttributeAssignmentResultsOptInOffOptOutOnTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.optIn-off.optOut-on",
                WsGetAttributeAssignmentsResults.class));
    }

    public GroupAttributeResults getAttributeAssignmentResultsOptInOnOptOutOffTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.optIn-on.optOut-off",
                WsGetAttributeAssignmentsResults.class));
    }

    public GroupAttributeResults getAttributeAssignmentResultsOptInOffOptOutOffTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.optIn-off.optOut-off",
                WsGetAttributeAssignmentsResults.class));
    }

    public AttributeAssignmentsResults attributeAssignmentOptInResultTestData() {
        WsGetAttributeAssignmentsResults wsResults =
                getWsResultTestData("attribute.assignment.opt.in.result", WsGetAttributeAssignmentsResults.class);
        return new AttributeAssignmentsResults(wsResults);
    }

    public AttributeAssignmentsResults attributeAssignmentOptOutResultTestData() {
        WsGetAttributeAssignmentsResults wsResults =
                getWsResultTestData("attribute.assignment.opt.out.result", WsGetAttributeAssignmentsResults.class);
        return new AttributeAssignmentsResults(wsResults);
    }

    public AttributeAssignmentsResults attributeAssignmentEmptyResultTestData() {
        WsGetAttributeAssignmentsResults wsResults =
                getWsResultTestData("attribute.assignment.empty.result", WsGetAttributeAssignmentsResults.class);
        return new AttributeAssignmentsResults(wsResults);
    }

    public GroupAttribute attributeAssignSuccessTestData() {
        return new GroupAttribute(getWsResultTestData("ws.attribute.assign.success", WsAttributeAssign.class));
    }

    public GroupAttribute attributeAssignFailureTestData() {
        return new GroupAttribute(getWsResultTestData("ws.attribute.assign.failure", WsAttributeAssign.class));
    }
}
