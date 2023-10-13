package edu.hawaii.its.api.service;

import edu.hawaii.its.api.type.GroupType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.util.JsonUtil;
import edu.hawaii.its.api.util.PropertyLocator;
import edu.hawaii.its.api.wrapper.HasMembersResults;

import edu.internet2.middleware.grouperClient.ws.beans.WsHasMemberResults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberServiceTest {

    @Value("${groupings.api.test.uh-usernames}")
    private List<String> TEST_USERNAMES;
    
    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    private String groupingPath = "grouping-path";

    @SpyBean
    private GrouperApiService grouperApiService;

    @Autowired
    private MemberService memberService;

    private PropertyLocator propertyLocator;

    @BeforeEach
    public void beforeEach() throws Exception {
        propertyLocator = new PropertyLocator("src/test/resources", "grouper.test.properties");
    }

    @Test
    public void isAdminTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(GROUPING_ADMINS, TEST_USERNAMES.get(0));

        assertTrue(memberService.isAdmin(TEST_USERNAMES.get(0)));
    }

    @Test
    public void isOwnerTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(groupingPath + GroupType.OWNERS.value(), TEST_USERNAMES.get(0));
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(OWNERS_GROUP, TEST_USERNAMES.get(0));

        assertTrue(memberService.isOwner(groupingPath, TEST_USERNAMES.get(0)));
        assertTrue(memberService.isOwner(TEST_USERNAMES.get(0)));
    }

    @Test
    public void isIncludeTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(groupingPath + GroupType.INCLUDE.value(), TEST_USERNAMES.get(0));

        assertTrue(memberService.isInclude(groupingPath, TEST_USERNAMES.get(0)));
    }

    @Test
    public void isExcludeTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(groupingPath + GroupType.EXCLUDE.value(), TEST_USERNAMES.get(0));

        assertTrue(memberService.isExclude(groupingPath, TEST_USERNAMES.get(0)));
    }

    @Test
    public void isMemberTest() {
        String json = propertyLocator.find("ws.has.member.results.is.members.uhuuid");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(groupingPath, TEST_USERNAMES.get(0));

        assertTrue(memberService.isMember(groupingPath, TEST_USERNAMES.get(0)));
    }

    @Test
    public void isMemberNullTest() {
        String json = propertyLocator.find("ws.has.member.results.null.group");
        WsHasMemberResults wsHasMemberResults = JsonUtil.asObject(json, WsHasMemberResults.class);
        HasMembersResults hasMembersResults = new HasMembersResults(wsHasMemberResults);
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperApiService)
                .hasMemberResults(groupingPath, TEST_USERNAMES.get(0));

        assertFalse(memberService.isMember(groupingPath, TEST_USERNAMES.get(0)));
    }

}
