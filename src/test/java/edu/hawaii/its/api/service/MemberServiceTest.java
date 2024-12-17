package edu.hawaii.its.api.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.ActiveProfiles;

import edu.hawaii.its.api.configuration.GroupingsTestConfiguration;
import edu.hawaii.its.api.configuration.SpringBootWebApplication;
import edu.hawaii.its.api.type.GroupType;
import edu.hawaii.its.api.wrapper.HasMembersResults;

@ActiveProfiles("localTest")
@SpringBootTest(classes = { SpringBootWebApplication.class })
public class MemberServiceTest {

    @Value("${groupings.api.test.uids}")
    private List<String> TEST_UIDS;

    @Value("${groupings.api.grouping_admins}")
    private String GROUPING_ADMINS;

    @Value("${groupings.api.grouping_owners}")
    private String OWNERS_GROUP;

    private String groupingPath = "grouping-path";

    @MockitoSpyBean
    private GrouperService grouperService;

    @Autowired
    private MemberService memberService;

    @Autowired
    private GroupingsTestConfiguration groupingsTestConfiguration;

    @Test
    public void isAdminTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(GROUPING_ADMINS, TEST_UIDS.get(0));

        assertTrue(memberService.isAdmin(TEST_UIDS.get(0)));
    }

    @Test
    public void isOwnerTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupingPath + GroupType.OWNERS.value(), TEST_UIDS.get(0));
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(OWNERS_GROUP, TEST_UIDS.get(0));

        assertTrue(memberService.isOwner(groupingPath, TEST_UIDS.get(0)));
        assertTrue(memberService.isOwner(TEST_UIDS.get(0)));
    }

    @Test
    public void isIncludeTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupingPath + GroupType.INCLUDE.value(), TEST_UIDS.get(0));

        assertTrue(memberService.isInclude(groupingPath, TEST_UIDS.get(0)));
    }

    @Test
    public void isExcludeTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupingPath + GroupType.EXCLUDE.value(), TEST_UIDS.get(0));

        assertTrue(memberService.isExclude(groupingPath, TEST_UIDS.get(0)));
    }

    @Test
    public void isMemberTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsIsMembersUhuuidTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupingPath, TEST_UIDS.get(0));

        assertTrue(memberService.isMember(groupingPath, TEST_UIDS.get(0)));
    }

    @Test
    public void isMemberNullTest() {
        HasMembersResults hasMembersResults = groupingsTestConfiguration.hasMemberResultsNullGroupTestData();
        assertNotNull(hasMembersResults);
        doReturn(hasMembersResults).when(grouperService)
                .hasMemberResults(groupingPath, TEST_UIDS.get(0));

        assertFalse(memberService.isMember(groupingPath, TEST_UIDS.get(0)));
    }

}
