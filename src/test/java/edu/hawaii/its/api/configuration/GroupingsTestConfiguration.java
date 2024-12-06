package edu.hawaii.its.api.configuration;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.test.context.TestConfiguration;

import edu.hawaii.its.api.groupings.GroupingSyncDestination;
import edu.hawaii.its.api.type.OotbActiveProfile;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AddMemberResult;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributeResult;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.FindAttributesResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttribute;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMemberResult;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.Subject;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import edu.hawaii.its.api.wrapper.UpdatedTimestampResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributeResult;
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
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubject;

@TestConfiguration
public class GroupingsTestConfiguration {

    public static final Log log = LogFactory.getLog(GroupingsTestConfiguration.class);
    private final PropertyLocator propertyLocator =
            new PropertyLocator("src/test/resources", "grouper.test.properties");

    private <T> T getWsResultTestData(String propertyName, Class<T> type) {
        String json = propertyLocator.find(propertyName);
        return JsonUtil.asObject(json, type);
    }

    public List<OotbActiveProfile> ootbActiveProfilesTestData() {
        return JsonUtil.asListFromFile("ootb.active.user.profiles.json", OotbActiveProfile.class);
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

    public HasMembersResults hasMemberResultNullSubjectResultCodeTestData() {
        return new HasMembersResults(
                getWsResultTestData("ws.has.member.result.null.subject.result.code", WsHasMemberResults.class));
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

    public AddMembersResults addMemberResultsResetGroupTestData() {
        return new AddMembersResults(
                getWsResultTestData("ws.add.member.results.reset.group", WsAddMemberResults.class));
    }

    public AddMembersResults addMemberResultsSuccessIncludeTimestampTestData() {
        return new AddMembersResults(
                getWsResultTestData("ws.add.member.results.success.include.timestamp", WsAddMemberResults.class));
    }

    public AddMembersResults addMemberResultsSuccessOwnersTimestampTestData() {
        return new AddMembersResults(
                getWsResultTestData("ws.add.member.results.success.owners.timestamp", WsAddMemberResults.class));
    }

    public AddMembersResults addMemberResultsSuccessAdminTimestampTestData() {
        return new AddMembersResults(
                getWsResultTestData("ws.add.member.results.success.admin.timestamp", WsAddMemberResults.class));
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

    public SubjectsResults getSubjectResultUhuuidFailureTestData() {
        return new SubjectsResults(
                getWsResultTestData("ws.get.subject.result.uhuuid.failure", WsGetSubjectsResults.class));
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

    public GroupSaveResults groupSaveResultsDescriptionUpdatedTestData() {
        return new GroupSaveResults(
                getWsResultTestData("ws.group.save.results.description.updated", WsGroupSaveResults.class));
    }

    public GroupSaveResults groupSaveResultsDescriptionNotUpdatedTestData() {
        return new GroupSaveResults(
                getWsResultTestData("ws.group.save.results.description.not.updated", WsGroupSaveResults.class));
    }

    public GroupSaveResults groupSaveResultsDescriptionEmptyResultsTestData() {
        return new GroupSaveResults(
                getWsResultTestData("ws.group.save.results.description.empty.results", WsGroupSaveResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsTimeChangedTestData() {
        return new UpdatedTimestampResults(
                getWsResultTestData("ws.assign.attributes.results.time.changed", WsAssignAttributesResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsMultipleTimeChangedTestData() {
        return new UpdatedTimestampResults(getWsResultTestData("ws.assign.attributes.results.multiple.time.changed",
                WsAssignAttributesResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsTimeNotChangedTestData() {
        return new UpdatedTimestampResults(
                getWsResultTestData("ws.assign.attributes.results.time.not.changed", WsAssignAttributesResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsMultipleTimeNotChangedTestData() {
        return new UpdatedTimestampResults(getWsResultTestData("ws.assign.attributes.results.multiple.time.not.changed",
                WsAssignAttributesResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsTimeEmptyGroupsEmptyResultsTestData() {
        return new UpdatedTimestampResults(
                getWsResultTestData("ws.assign.attributes.results.time.empty.groups.empty.results",
                        WsAssignAttributesResults.class));
    }

    public UpdatedTimestampResults assignAttributesResultsTimeEmptyValuesTestData() {
        return new UpdatedTimestampResults(
                getWsResultTestData("ws.assign.attributes.results.time.empty.values", WsAssignAttributesResults.class));
    }

    public AssignAttributeResult assignAttributesResultsTurnOffOptInSuccessTestData() {
        return new AssignAttributeResult(getWsResultTestData("ws.assign.attributes.results.turn.off.opt.in.success",
                WsAssignAttributeResult.class));
    }

    public AssignAttributesResults assignAttributesResultsNullAssignAttributeResultTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.null.assign.attribute.result",
                        WsAssignAttributesResults.class));
    }

    public AssignAttributesResults assignAttributesResultsChangedTrueTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.changed.true", WsAssignAttributesResults.class));
    }

    public AssignAttributesResults assignAttributesResultsChangedFalseTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.changed.false", WsAssignAttributesResults.class));
    }

    public AssignAttributesResults assignAttributesResultsDeletedTrueTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.deleted.true", WsAssignAttributesResults.class));
    }

    public AssignAttributesResults assignAttributesResultsDeletedFalseTestData() {
        return new AssignAttributesResults(
                getWsResultTestData("ws.assign.attributes.results.deleted.false", WsAssignAttributesResults.class));
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

    public GroupAttributeResults getAttributeAssignmentResultsFailureTestData() {
        return new GroupAttributeResults(getWsResultTestData("ws.get.attribute.assignment.results.failure",
                WsGetAttributeAssignmentsResults.class));
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

    public GroupAttribute attributeAssignSuccessTestData() {
        return new GroupAttribute(getWsResultTestData("ws.attribute.assign.success", WsAttributeAssign.class));
    }

    public GroupAttribute attributeAssignFailureTestData() {
        return new GroupAttribute(getWsResultTestData("ws.attribute.assign.failure", WsAttributeAssign.class));
    }
}