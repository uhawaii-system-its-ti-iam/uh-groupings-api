package edu.hawaii.its.api.configuration;

import edu.hawaii.its.api.service.GrouperApiOOTBService;
import edu.hawaii.its.api.service.GrouperApiService;
import edu.hawaii.its.api.service.GrouperService;
import edu.hawaii.its.api.service.MemberService;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.AddMembersResults;
import edu.hawaii.its.api.wrapper.FindGroupsResults;
import edu.hawaii.its.api.wrapper.HasMembersResults;
import edu.hawaii.its.api.wrapper.SubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsAddMemberResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsFindGroupsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetSubjectsResults;
import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
class GroupingPropertyConfigurer {

    private PropertyLocator propertyLocator = new PropertyLocator("src/main/resources", "data.harness.properties");

    public static final Log log = LogFactory.getLog(GroupingPropertyConfigurer.class);
    /*
    Data Harness Configuration
     */

    @Bean(name = "HasMembersResultsOOTBBean")
//    @ConditionalOnProperty(name = "grouper.hasMembersResults.enabled", havingValue = "true")
    public HasMembersResults grouperHasMembersResultsOOTB() throws IOException {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        return hasMembersResults;
    }

    @Bean(name = "AddMemberResultsOOTBBean")
//    @ConditionalOnProperty(name = "grouper.addMemberResults.enabled", havingValue = "true")
    public AddMembersResults grouperAddMemberResultsOOTB() throws IOException {
        String json = propertyLocator.find("ws.add.member.results.success");
        WsAddMemberResults wsAddMemberResults = JsonUtil.asObject(json, WsAddMemberResults.class);
        AddMembersResults addMemberResults = new AddMembersResults(wsAddMemberResults);
        return addMemberResults;
    }

    @Bean(name = "GetSubjectsResultsOOTBBean")
//    @ConditionalOnProperty(name = "grouper.getSubjectsResults.enabled", havingValue = "true")
    public SubjectsResults grouperGetSubjectsResultsOOTB() throws IOException {
        String json = propertyLocator.find("ws.get.subjects.results.success");
        WsGetSubjectsResults wsGetSubjectsResults = JsonUtil.asObject(json, WsGetSubjectsResults.class);
        SubjectsResults getSubjectsResults = new SubjectsResults(wsGetSubjectsResults);
        return getSubjectsResults;
    }

    @Bean(name = "FindGroupsResultsOOTBBean")
//    @ConditionalOnProperty(name = "grouper.findGroupsResults.enabled", havingValue = "true")
    public FindGroupsResults grouperFindGroupsResultsOOTB() throws IOException {
        String json = propertyLocator.find("find.groups.results.description");
        WsFindGroupsResults wsFindGroupsResults = JsonUtil.asObject(json, WsFindGroupsResults.class);
        FindGroupsResults findGroupsResults = new FindGroupsResults(wsFindGroupsResults);
        return findGroupsResults;
    }



    /*
    Service Configuration
     */

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "OOTB")
    public GrouperService grouperApiOOTBService() {
        log.debug("OOTB Grouper Api Service Started");
        return new GrouperApiOOTBService();
    }

    @Bean(name = "grouperService")
    @ConditionalOnProperty(name = "grouping.api.server.type", havingValue = "REAL")
    public GrouperService grouperApiService() {
        log.debug("REAL Grouper Api Service Started");
        return new GrouperApiService();
    }

}
