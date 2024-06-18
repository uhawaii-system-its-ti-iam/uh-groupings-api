package edu.hawaii.its.api.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GrouperService;
import edu.hawaii.its.api.service.OotbGrouperApiService;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.AssignAttributesResults;
import edu.hawaii.its.api.wrapper.AssignGrouperPrivilegesResult;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.GetGroupsResults;
import edu.hawaii.its.api.wrapper.GetMembersResults;
import edu.hawaii.its.api.wrapper.GroupAttributeResults;
import edu.hawaii.its.api.wrapper.GroupSaveResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.RemoveMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignAttributesResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAssignGrouperPrivilegesLiteResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsDeleteMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetAttributeAssignmentsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetMembersResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroupSaveResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

@Configuration
@ActiveProfiles("ootb")
class OotbGrouperPropertyConfigurer {

    private PropertyLocator propertyLocator = new PropertyLocator("src/main/resources", "data.harness.properties");

    public static final Log log = LogFactory.getLog(OotbGrouperPropertyConfigurer.class);

    /*
    Data Harness Configuration
     */

    @Bean(name = "HasMembersResultsOOTBBean")
    public HasMembersResults grouperHasMembersResultsOOTB() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        return hasMembersResults;
    }

    @Bean(name = "AddMemberResultsOOTBBean")
    public AddMembersResults grouperAddMemberResultsOOTB() {
        String json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMemberResults = new AddMembersResults(wsAddMemberResults);
        return addMemberResults;
    }

    @Bean(name = "GetSubjectsResultsOOTBBean")
    public SubjectsResults grouperGetSubjectsResultsOOTB() {
        String json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults getSubjectsResults = new SubjectsResults(wsGetSubjectsResults);
        return getSubjectsResults;
    }

    @Bean(name = "FindGroupsResultsOOTBBean")
    public FindGroupsResults grouperFindGroupsResultsOOTB() {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        return findGroupsResults;
    }

    @Bean(name = "GroupSaveResultsOOTBBean")
    public GroupSaveResults grouperGroupSaveResultsOOTB() {
        String json = propertyLocator.find("ws.group.save.results.description.updated");
        WsGroupSaveResults wsGroupSaveResults = JsonUtil.asObject(json, WsGroupSaveResults.class);
        GroupSaveResults groupSaveResults = new GroupSaveResults(wsGroupSaveResults);
        return groupSaveResults;
    }

    @Bean(name = "AssignAttributesOOTBBean")
    public AssignAttributesResults grouperAssignAttributesResultsOOTB() {
        String json = propertyLocator.find("ws.assign.attributes.results.time.changed");
        WsAssignAttributesResults wsAssignAttributesResults = JsonUtil.asObject(json, WsAssignAttributesResults.class);
        AssignAttributesResults assignAttributesResults = new AssignAttributesResults(wsAssignAttributesResults);
        return assignAttributesResults;
    }

    @Bean(name = "GetMembersResultsOOTBBean")
    public GetMembersResults grouperGetMembersResultsOOTB() {
        String json = propertyLocator.find("ws.get.members.results.success.multiple.groups");
        WsGetMembersResults wsGetMembersResults = JsonUtil.asObject(json, WsGetMembersResults.class);
        GetMembersResults getMembersResults = new GetMembersResults(wsGetMembersResults);
        return getMembersResults;
    }

    @Bean(name = "RemoveMembersResultsOOTBBean")
    public RemoveMembersResults grouperRemoveMembersResultsOOTB() {
        String json = propertyLocator.find("ws.delete.member.results.success");
        WsDeleteMemberResults wsDeleteMemberResults = JsonUtil.asObject(json, WsDeleteMemberResults.class);
        RemoveMembersResults removeMembersResults = new RemoveMembersResults(wsDeleteMemberResults);
        return removeMembersResults;
    }
    @Bean(name = "AttributeAssignmentResultsOOTBBean")
    public GroupAttributeResults grouperGroupAttributeResultsOOTB() {
        String json = propertyLocator.find("ws.get.attribute.assignment.results.success");
        WsGetAttributeAssignmentsResults wsGetAttributeAssignmentsResults = JsonUtil.asObject(json, WsGetAttributeAssignmentsResults.class);
        GroupAttributeResults groupAttributeResults = new GroupAttributeResults(wsGetAttributeAssignmentsResults);
        return groupAttributeResults;
    }
    @Bean(name = "AssignGrouperPrivilegesResultOOTBBean")
    public AssignGrouperPrivilegesResult grouperAssignGrouperPrivilegesResultOOTB() {
        String json = propertyLocator.find("ws.assign.grouper.privileges.results.success");
        WsAssignGrouperPrivilegesLiteResult wsAssignGrouperPrivilegesLiteResult = JsonUtil.asObject(json, WsAssignGrouperPrivilegesLiteResult.class);
        AssignGrouperPrivilegesResult assignGrouperPrivilegesResult = new AssignGrouperPrivilegesResult(wsAssignGrouperPrivilegesLiteResult);
        return assignGrouperPrivilegesResult;
    }
    @Bean(name = "GetGroupsResultsOOTBBean")
    public GetGroupsResults grouperGetGroupsResultsOOTB() {
        String json = propertyLocator.find("ws.get.groups.results.success");
        WsGetGroupsResults wsgetGroupsResults = JsonUtil.asObject(json, WsGetGroupsResults.class);
        GetGroupsResults getGroupsResults = new GetGroupsResults(wsgetGroupsResults);
        return getGroupsResults;
    }

    /*
    Service Configuration
     */

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "OOTB")
    public GrouperService grouperApiOOTBService() {
        log.debug("OOTB Grouper Api Service Started");
        return new OotbGrouperApiService();
    }

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "GROUPER", matchIfMissing = true)
    public GrouperService grouperApiService() {
        log.debug("REAL Grouper Api Service Started");
        return new GrouperApiService();
    }

}


