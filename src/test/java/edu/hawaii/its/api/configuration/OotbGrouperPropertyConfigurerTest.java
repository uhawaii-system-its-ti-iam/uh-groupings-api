package edu.hawaii.its.api.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import edu.hawaii.its.api.service.GrouperService;
import edu.hawaii.its.api.service.OotbGrouperApiService;
import edu.hawaii.its.api.service.OotbGroupingPropertiesService;
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

@SpringBootTest(classes = { SpringBootWebApplication.class })
@ActiveProfiles("ootb")
@TestPropertySource(locations = "classpath:ootb.grouper.test.properties")
public class OotbGrouperPropertyConfigurerTest {

    @Autowired
    private ApplicationContext context;

    @MockBean
    private OotbGroupingPropertiesService ootbGroupingPropertiesService;

    @DynamicPropertySource
    static void grouperProperties(DynamicPropertyRegistry registry) {
        registry.add("grouping.api.server.type", () -> "OOTB");
    }

    @Test
    public void testHasMembersResultsOOTBBean() {
        HasMembersResults bean = context.getBean("HasMembersResultsOOTBBean", HasMembersResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getGroup());
        assertNotNull(bean.getGroupPath());
        assertNotNull(bean.getResults());
    }

    @Test
    public void testAddMemberResultsOOTBBean() {
        AddMembersResults bean = context.getBean("AddMemberResultsOOTBBean", AddMembersResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getResults());
    }

    @Test
    public void testGetSubjectsResultsOOTBBean() {
        SubjectsResults bean = context.getBean("GetSubjectsResultsOOTBBean", SubjectsResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getSubjects());
        assertNotNull(bean.getResultCode());
    }

    @Test
    public void testFindGroupsResultsOOTBBean() {
        FindGroupsResults bean = context.getBean("FindGroupsResultsOOTBBean", FindGroupsResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getGroup());
        assertNotNull(bean.getGroups());
        assertNotNull(bean.getResultCode());
    }

    @Test
    public void testGroupSaveResultsOOTBBean() {
        GroupSaveResults bean = context.getBean("GroupSaveResultsOOTBBean", GroupSaveResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getResultCode());
        assertNotNull(bean.getGroup());
    }

    @Test
    public void testAssignAttributesOOTBBean() {
        AssignAttributesResults bean = context.getBean("AssignAttributesOOTBBean", AssignAttributesResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getAttributesResults());
        assertNotNull(bean.getAssignAttributeResults());
    }

    @Test
    public void testGetMembersResultsOOTBBean() {
        GetMembersResults bean = context.getBean("GetMembersResultsOOTBBean", GetMembersResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getMembersResults());
    }

    @Test
    public void testRemoveMembersResultsOOTBBean() {
        RemoveMembersResults bean = context.getBean("RemoveMembersResultsOOTBBean", RemoveMembersResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getResults());
        assertNotNull(bean.getResultCode());
        assertNotNull(bean.getGroup());
        assertNotNull(bean.getGroupPath());
    }

    @Test
    public void testAttributeAssignmentResultsOOTBBean() {
        GroupAttributeResults bean = context.getBean("AttributeAssignmentResultsOOTBBean", GroupAttributeResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getGroups());
        assertNotNull(bean.getGroupAttributes());
        assertNotNull(bean.getAttributesResults());
    }

    @Test
    public void testAssignGrouperPrivilegesResultOOTBBean() {
        AssignGrouperPrivilegesResult bean =
                context.getBean("AssignGrouperPrivilegesResultOOTBBean", AssignGrouperPrivilegesResult.class);
        assertNotNull(bean);
        assertNotNull(bean.getGroup());
        assertNotNull(bean.getResultCode());
        assertNotNull(bean.getPrivilegeName());
        assertNotNull(bean.getSubject());
        assertNotNull(bean.isAllowed());
    }

    @Test
    public void testGetGroupsResultsOOTBBean() {
        GetGroupsResults bean = context.getBean("GetGroupsResultsOOTBBean", GetGroupsResults.class);
        assertNotNull(bean);
        assertNotNull(bean.getGroups());
        assertNotNull(bean.getResultCode());
        assertNotNull(bean.getSubject());
    }

    @Test
    public void testGrouperApiOOTBService() {
        GrouperService service = context.getBean("grouperService", GrouperService.class);
        assertNotNull(service);
        assertTrue(service instanceof OotbGrouperApiService);
    }
}
